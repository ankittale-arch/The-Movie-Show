package com.ankitt.themovieshow.feature.recentlyviewed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.components.RatingBadge
import com.ankitt.themovieshow.core.designsystem.text.clipToWords
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme

@Composable
fun RecentlyViewedScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    viewModel: RecentlyViewedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecentlyViewedContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick,
        onClearHistoryClick = viewModel::clearHistory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentlyViewedContent(
    uiState: RecentlyViewedUiState,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    onClearHistoryClick: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Recently Viewed") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.movies.isNotEmpty()) {
                        IconButton(onClick = onClearHistoryClick) {
                            Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = "Clear history")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.movies.isEmpty()) {
                Text(
                    text = "No movies viewed yet",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                RecentlyViewedGrid(items = uiState.movies, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun RecentlyViewedGrid(items: List<RecentlyViewedItem>, onMovieClick: (Int) -> Unit) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { item ->
            RecentlyViewedCard(item = item, onClick = { onMovieClick(item.id) })
        }
    }
}

@Composable
private fun RecentlyViewedCard(item: RecentlyViewedItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (item.voteAverage > 0.0) {
                RatingBadge(
                    rating = item.voteAverage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
        }
        Text(
            text = item.title.clipToWords(),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecentlyViewedContentPreview() {
    TheMovieShowTheme {
        RecentlyViewedContent(uiState = RecentlyViewedUiState(), onBackClick = {})
    }
}

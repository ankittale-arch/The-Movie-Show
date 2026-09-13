package com.ankitt.themovieshow.feature.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.components.RatingBadge
import com.ankitt.themovieshow.core.designsystem.text.clipToWords
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme

@Composable
fun BookmarksScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookmarksContent(uiState = uiState, onBackClick = onBackClick, onMovieClick = onMovieClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksContent(
    uiState: BookmarksUiState,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = remember { listOf("Favorites", "Watchlist") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Bookmarks") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val items = if (selectedTab == 0) uiState.favorites else uiState.watchlist
        val emptyMessage = if (selectedTab == 0) "No favorites yet" else "Nothing in your watchlist yet"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (items.isEmpty()) {
                Text(
                    text = emptyMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                BookmarksGrid(items = items, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun BookmarksGrid(items: List<BookmarkItem>, onMovieClick: (Int) -> Unit) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { item ->
            BookmarkCard(item = item, onClick = { onMovieClick(item.id) })
        }
    }
}

@Composable
private fun BookmarkCard(item: BookmarkItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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
private fun BookmarksContentPreview() {
    TheMovieShowTheme {
        BookmarksContent(uiState = BookmarksUiState(), onBackClick = {})
    }
}

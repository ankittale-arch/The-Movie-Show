package com.ankitt.themovieshow.feature.movielist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.animation.sharedMovieElement
import com.ankitt.themovieshow.core.designsystem.components.RatingBadge
import com.ankitt.themovieshow.core.designsystem.text.clipToWords
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme

/** How many items from the end of the loaded list triggers fetching the next page. */
private const val LOAD_MORE_THRESHOLD = 6

/**
 * Generic "see all" screen: a title plus a staggered grid of posters, paged in as the user
 * scrolls. Used both for a Home row's "more" link (e.g. `listKey = HomeListKeys.NOW_PLAYING`) and
 * for a genre chip (`listKey = HomeListKeys.genre(id)`) — the caller supplies [listKey] and
 * [title], everything else is identical.
 */
@Composable
fun MovieListScreen(
    listKey: String,
    title: String,
    onBackClick: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
    viewModel: MovieListViewModel = hiltViewModel(),
) {
    LaunchedEffect(listKey) {
        viewModel.load(listKey)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MovieListContent(
        title = title,
        uiState = uiState,
        onBackClick = onBackClick,
        onLoadMore = viewModel::loadMore,
        onMovieClick = onMovieClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MovieListContent(
    title: String,
    uiState: MovieListUiState,
    onBackClick: () -> Unit,
    onLoadMore: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            val errorMessage = uiState.errorMessage
            when {
                uiState.isLoading && uiState.movies.isEmpty() ->
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("movieListLoadingIndicator"),
                    )

                errorMessage != null && uiState.movies.isEmpty() -> Text(
                    text = errorMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )

                uiState.movies.isEmpty() -> Text(
                    text = "No movies found for \"$title\"",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                else -> MovieListGrid(uiState = uiState, onLoadMore = onLoadMore, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun MovieListGrid(
    uiState: MovieListUiState,
    onLoadMore: () -> Unit,
    onMovieClick: (Int) -> Unit = {},
) {
    val gridState = rememberLazyStaggeredGridState()

    // Deliberately reads gridState.layoutInfo (live Compose-tracked state) rather than
    // uiState.movies.size: a `remember` with no key only evaluates its initializer once, so a
    // derivedStateOf capturing a plain parameter like uiState would freeze at whatever `movies`
    // was on first composition (often still empty) and never notice the list growing.
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(shouldLoadMore, uiState.hasMorePages) {
        if (shouldLoadMore && uiState.hasMorePages) {
            onLoadMore()
        }
    }

    LazyVerticalStaggeredGrid(
        state = gridState,
        // Adaptive rather than a fixed column count: 2 columns on a typical phone width, but
        // more columns appear automatically as the available width grows (tablets, landscape,
        // foldables) instead of a couple of giant posters stretched across the screen.
        columns = StaggeredGridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(uiState.movies, key = { it.id }) { movie ->
            MovieListCard(movie = movie, onClick = { onMovieClick(movie.id) })
        }
        if (uiState.isLoadingMore) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieListCard(movie: MovieListItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedMovieElement(
                        key = "movie-poster-${movie.id}",
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    ),
            )
            if (movie.voteAverage > 0.0) {
                RatingBadge(
                    rating = movie.voteAverage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
        }
        Text(
            text = movie.title.clipToWords(),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieListContentPreview() {
    TheMovieShowTheme {
        MovieListContent(title = "Action", uiState = MovieListUiState(), onBackClick = {}, onLoadMore = {})
    }
}

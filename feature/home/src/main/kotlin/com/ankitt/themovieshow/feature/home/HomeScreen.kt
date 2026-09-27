package com.ankitt.themovieshow.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ankitt.themovieshow.core.data.HomeListKeys
import com.ankitt.themovieshow.core.designsystem.components.SyncStatusBanner
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme
import com.ankitt.themovieshow.feature.home.components.*

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onMovieListClick: (listKey: String, title: String) -> Unit = { _, _ -> },
    onMovieClick: (Int) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onSearchClick = onSearchClick,
        onBookmarksClick = onBookmarksClick,
        onRecentlyViewedClick = onRecentlyViewedClick,
        onMovieListClick = onMovieListClick,
        onMovieClick = onMovieClick,
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    onSearchClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
    onMovieListClick: (listKey: String, title: String) -> Unit = { _, _ -> },
    onMovieClick: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    // Compress the bottom nav bar down to just the selected tab while the user scrolls down
    // through the feed, and restore it as soon as they scroll back up. The connection only
    // observes scroll deltas — it never consumes them — so the list and pull-to-refresh are
    // unaffected.
    var isNavBarExpanded by rememberSaveable { mutableStateOf(true) }
    val navBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                when {
                    available.y < -NAV_BAR_TOGGLE_THRESHOLD_PX -> isNavBarExpanded = false
                    available.y > NAV_BAR_TOGGLE_THRESHOLD_PX -> isNavBarExpanded = true
                }
                return Offset.Zero
            }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { HomeTopAppBar() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(navBarScrollConnection),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SyncStatusBanner(
                    isOffline = uiState.isOffline,
                    isStale = uiState.isStale,
                    lastSyncedAtEpochMillis = uiState.lastSyncedAtEpochMillis,
                )
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        item {
                            HeroCarousel(
                                movies = uiState.heroMovies,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onMovieClick = onMovieClick,
                            )
                        }
                        item {
                            Column {
                                Text(
                                    text = "Genres",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    GenreChipRow(
                                        genres = uiState.genres,
                                        onGenreClick = { genre ->
                                            onMovieListClick(HomeListKeys.genre(genre.id), genre.name)
                                        },
                                    )
                                }
                            }
                        }
                        item {
                            MovieSection(
                                title = "Now Playing",
                                movies = uiState.nowPlaying,
                                variant = MovieCardVariant.PosterOnly,
                                onMoreClick = { onMovieListClick(HomeListKeys.NOW_PLAYING, "Now Playing") },
                                onMovieClick = onMovieClick,
                            )
                        }
                        item {
                            MovieSection(
                                title = "Popular Movies",
                                movies = uiState.popular,
                                variant = MovieCardVariant.PosterOnly,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.height(16.dp),
                                    )
                                },
                                onMoreClick = { onMovieListClick(HomeListKeys.POPULAR, "Popular Movies") },
                                onMovieClick = onMovieClick,
                            )
                        }
                        item {
                            MovieSection(
                                title = "Discover Movies",
                                movies = uiState.discover,
                                variant = MovieCardVariant.LandscapeOverlay,
                                onMoreClick = { onMovieListClick(HomeListKeys.DISCOVER, "Discover Movies") },
                                onMovieClick = onMovieClick,
                            )
                        }
                        item {
                            MovieSection(
                                title = "Upcoming Movies",
                                movies = uiState.upcoming,
                                variant = MovieCardVariant.PosterWithCaption,
                                onMoreClick = { onMovieListClick(HomeListKeys.UPCOMING, "Upcoming Movies") },
                                onMovieClick = onMovieClick,
                            )
                        }
                    }
                }
            }
            HomeBottomNavBar(
                selectedTab = HomeBottomNavTab.Home,
                expanded = isNavBarExpanded,
                onTabSelected = { tab ->
                    when (tab) {
                        // Tapping the compressed pill brings the full bar back.
                        HomeBottomNavTab.Home -> isNavBarExpanded = true
                        HomeBottomNavTab.Search -> onSearchClick()
                        HomeBottomNavTab.Bookmarks -> onBookmarksClick()
                        HomeBottomNavTab.Recent -> onRecentlyViewedClick()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/** Minimum per-frame scroll delta (in px) that flips the bottom nav bar, so tiny jitters don't. */
private const val NAV_BAR_TOGGLE_THRESHOLD_PX = 4f

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    TheMovieShowTheme {
        HomeContent(uiState = HomeUiState())
    }
}

package com.ankitt.themovieshow.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import com.ankitt.themovieshow.core.designsystem.animation.LocalSharedTransitionScope
import com.ankitt.themovieshow.feature.bookmarks.navigation.*
import com.ankitt.themovieshow.feature.home.navigation.*
import com.ankitt.themovieshow.feature.moviedetail.navigation.*
import com.ankitt.themovieshow.feature.movielist.navigation.*
import com.ankitt.themovieshow.feature.recentlyviewed.navigation.*
import com.ankitt.themovieshow.feature.search.navigation.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(HomeRoute)
    val onMovieClick: (Int) -> Unit = { movieId -> backStack.add(MovieDetailRoute(movieId)) }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                sharedTransitionScope = this,
                entryProvider = entryProvider {
                    homeEntry(
                        onSearchClick = { backStack.add(SearchRoute) },
                        onBookmarksClick = { backStack.add(BookmarksRoute) },
                        onRecentlyViewedClick = { backStack.add(RecentlyViewedRoute) },
                        onMovieListClick = { listKey, title -> backStack.add(MovieListRoute(listKey, title)) },
                        onMovieClick = onMovieClick,
                    )
                    searchEntry(onBackClick = { backStack.removeLastOrNull() }, onMovieClick = onMovieClick)
                    movieListEntry(onBackClick = { backStack.removeLastOrNull() }, onMovieClick = onMovieClick)
                    movieDetailEntry(onBackClick = { backStack.removeLastOrNull() })
                    bookmarksEntry(onBackClick = { backStack.removeLastOrNull() }, onMovieClick = onMovieClick)
                    recentlyViewedEntry(onBackClick = { backStack.removeLastOrNull() }, onMovieClick = onMovieClick)
                },
            )
        }
    }
}

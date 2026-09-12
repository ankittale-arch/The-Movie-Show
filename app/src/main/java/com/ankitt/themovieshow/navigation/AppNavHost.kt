package com.ankitt.themovieshow.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ankitt.themovieshow.core.designsystem.animation.LocalSharedTransitionScope
import com.ankitt.themovieshow.feature.home.navigation.HomeRoute
import com.ankitt.themovieshow.feature.home.navigation.homeEntry
import com.ankitt.themovieshow.feature.moviedetail.navigation.MovieDetailRoute
import com.ankitt.themovieshow.feature.moviedetail.navigation.movieDetailEntry
import com.ankitt.themovieshow.feature.movielist.navigation.MovieListRoute
import com.ankitt.themovieshow.feature.movielist.navigation.movieListEntry
import com.ankitt.themovieshow.feature.search.navigation.SearchRoute
import com.ankitt.themovieshow.feature.search.navigation.searchEntry

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
                        onMovieListClick = { listKey, title -> backStack.add(MovieListRoute(listKey, title)) },
                        onMovieClick = onMovieClick,
                    )
                    searchEntry(onBackClick = { backStack.removeLastOrNull() }, onMovieClick = onMovieClick)
                    movieListEntry(onBackClick = { backStack.removeLastOrNull() }, onMovieClick = onMovieClick)
                    movieDetailEntry(onBackClick = { backStack.removeLastOrNull() })
                },
            )
        }
    }
}

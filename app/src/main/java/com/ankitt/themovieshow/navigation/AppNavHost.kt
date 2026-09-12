package com.ankitt.themovieshow.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ankitt.themovieshow.feature.home.navigation.HomeRoute
import com.ankitt.themovieshow.feature.home.navigation.homeEntry
import com.ankitt.themovieshow.feature.movielist.navigation.MovieListRoute
import com.ankitt.themovieshow.feature.movielist.navigation.movieListEntry
import com.ankitt.themovieshow.feature.search.navigation.SearchRoute
import com.ankitt.themovieshow.feature.search.navigation.searchEntry

@Composable
fun AppNavHost() {
    val backStack = rememberNavBackStack(HomeRoute)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            homeEntry(
                onSearchClick = { backStack.add(SearchRoute) },
                onMovieListClick = { listKey, title -> backStack.add(MovieListRoute(listKey, title)) },
            )
            searchEntry(onBackClick = { backStack.removeLastOrNull() })
            movieListEntry(onBackClick = { backStack.removeLastOrNull() })
        },
    )
}

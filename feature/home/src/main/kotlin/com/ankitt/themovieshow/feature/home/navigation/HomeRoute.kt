package com.ankitt.themovieshow.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ankitt.themovieshow.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onSearchClick: () -> Unit,
    onMovieListClick: (listKey: String, title: String) -> Unit,
) {
    entry<HomeRoute> {
        HomeScreen(onSearchClick = onSearchClick, onMovieListClick = onMovieListClick)
    }
}

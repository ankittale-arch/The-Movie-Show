package com.ankitt.themovieshow.feature.recentlyviewed.navigation

import androidx.navigation3.runtime.*
import com.ankitt.themovieshow.feature.recentlyviewed.RecentlyViewedScreen
import kotlinx.serialization.Serializable

@Serializable
data object RecentlyViewedRoute : NavKey

fun EntryProviderScope<NavKey>.recentlyViewedEntry(onBackClick: () -> Unit, onMovieClick: (Int) -> Unit) {
    entry<RecentlyViewedRoute> {
        RecentlyViewedScreen(onBackClick = onBackClick, onMovieClick = onMovieClick)
    }
}

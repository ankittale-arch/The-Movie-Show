package com.ankitt.themovieshow.feature.moviedetail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ankitt.themovieshow.feature.moviedetail.MovieDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailRoute(val movieId: Int) : NavKey

fun EntryProviderScope<NavKey>.movieDetailEntry(onBackClick: () -> Unit) {
    entry<MovieDetailRoute> { route ->
        MovieDetailScreen(movieId = route.movieId, onBackClick = onBackClick)
    }
}

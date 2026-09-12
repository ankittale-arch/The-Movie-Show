package com.ankitt.themovieshow.feature.movielist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ankitt.themovieshow.feature.movielist.MovieListScreen
import kotlinx.serialization.Serializable

/**
 * [listKey] is a [com.ankitt.themovieshow.core.data.HomeListKeys] value — a Home row's fixed key
 * (e.g. `HomeListKeys.NOW_PLAYING`) for a "more" link, or `HomeListKeys.genre(id)` for a genre
 * chip. [title] is just the screen's display title (e.g. "Now Playing" or the genre's name).
 */
@Serializable
data class MovieListRoute(val listKey: String, val title: String) : NavKey

fun EntryProviderScope<NavKey>.movieListEntry(onBackClick: () -> Unit) {
    entry<MovieListRoute> { route ->
        MovieListScreen(
            listKey = route.listKey,
            title = route.title,
            onBackClick = onBackClick,
        )
    }
}

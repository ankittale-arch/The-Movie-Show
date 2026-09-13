package com.ankitt.themovieshow.feature.bookmarks.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ankitt.themovieshow.feature.bookmarks.BookmarksScreen
import kotlinx.serialization.Serializable

@Serializable
data object BookmarksRoute : NavKey

fun EntryProviderScope<NavKey>.bookmarksEntry(onBackClick: () -> Unit, onMovieClick: (Int) -> Unit) {
    entry<BookmarksRoute> {
        BookmarksScreen(onBackClick = onBackClick, onMovieClick = onMovieClick)
    }
}

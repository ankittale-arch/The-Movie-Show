package com.ankitt.themovieshow.feature.bookmarks

data class BookmarksUiState(
    val favorites: List<BookmarkItem> = emptyList(),
    val watchlist: List<BookmarkItem> = emptyList(),
)

data class BookmarkItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double,
)

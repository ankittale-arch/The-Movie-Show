package com.ankitt.themovieshow.feature.recentlyviewed

data class RecentlyViewedUiState(
    val movies: List<RecentlyViewedItem> = emptyList(),
)

data class RecentlyViewedItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double,
)

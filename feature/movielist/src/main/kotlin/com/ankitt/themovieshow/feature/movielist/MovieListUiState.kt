package com.ankitt.themovieshow.feature.movielist

data class MovieListUiState(
    val movies: List<MovieListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val errorMessage: String? = null,
)

data class MovieListItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
)

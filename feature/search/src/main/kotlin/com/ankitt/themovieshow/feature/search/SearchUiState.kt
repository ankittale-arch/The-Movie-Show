package com.ankitt.themovieshow.feature.search

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResultMovie> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
) {
    val showPrompt: Boolean get() = query.isBlank()
    val showNoResults: Boolean
        get() = !isSearching && query.isNotBlank() && results.isEmpty() && errorMessage == null
}

data class SearchResultMovie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
)

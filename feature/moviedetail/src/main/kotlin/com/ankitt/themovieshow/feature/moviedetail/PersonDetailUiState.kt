package com.ankitt.themovieshow.feature.moviedetail

data class PersonDetailUiState(
    val person: PersonDetailUi? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class PersonDetailUi(
    val id: Int,
    val name: String,
    val biography: String,
    val meta: String?,
    val profileUrl: String?,
    val knownFor: List<KnownForMovieUi>,
)

data class KnownForMovieUi(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val year: String?,
)

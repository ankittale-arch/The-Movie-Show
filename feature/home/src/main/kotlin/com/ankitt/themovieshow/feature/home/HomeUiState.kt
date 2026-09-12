package com.ankitt.themovieshow.feature.home

data class HomeUiState(
    val heroMovies: List<HomeMovie> = emptyList(),
    val genres: List<HomeGenre> = emptyList(),
    val nowPlaying: List<HomeMovie> = emptyList(),
    val popular: List<HomeMovie> = emptyList(),
    val discover: List<HomeMovie> = emptyList(),
    val upcoming: List<HomeMovie> = emptyList(),
)

data class HomeMovie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
)

data class HomeGenre(
    val id: Int,
    val name: String,
)

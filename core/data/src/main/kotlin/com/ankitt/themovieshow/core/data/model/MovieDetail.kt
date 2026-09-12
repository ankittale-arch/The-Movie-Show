package com.ankitt.themovieshow.core.data.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val voteAverage: Double,
    val voteCount: Int,
    val tagline: String?,
    val originalLanguage: String?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
)

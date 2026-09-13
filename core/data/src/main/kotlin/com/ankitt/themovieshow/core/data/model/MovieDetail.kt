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
    val trailerYoutubeKey: String?,
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
)

data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String,
    val birthday: String?,
    val deathday: String?,
    val placeOfBirth: String?,
    val profilePath: String?,
    val knownForDepartment: String?,
    val knownFor: List<KnownForMovie>,
)

data class KnownForMovie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val releaseDate: String?,
)

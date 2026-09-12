package com.ankitt.themovieshow.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("overview") val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("popularity") val popularity: Double = 0.0,
)

/**
 * Shape shared by every TMDB list endpoint used for Phase 2 (now_playing, popular, upcoming,
 * trending, discover): a page of [MovieDto] plus TMDB's own pagination bookkeeping. `page`/
 * `totalPages` are unused until pagination lands (Phase 4) but are modeled now so the DTO shape
 * doesn't need to change later.
 */
@Serializable
data class MoviePageDto(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<MovieDto>,
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
)

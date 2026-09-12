package com.ankitt.themovieshow.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET movie/{id}?append_to_response=credits` — details and cast in one call. */
@Serializable
data class MovieDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("overview") val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("popularity") val popularity: Double = 0.0,
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("credits") val credits: CreditsDto = CreditsDto(),
)

@Serializable
data class CreditsDto(
    @SerialName("cast") val cast: List<CastMemberDto> = emptyList(),
)

@Serializable
data class CastMemberDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("character") val character: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("order") val order: Int = 0,
)

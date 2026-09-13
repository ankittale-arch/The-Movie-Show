package com.ankitt.themovieshow.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET person/{person_id}?append_to_response=movie_credits` — a cast member's bio and
 * filmography, tapped from a movie's cast row. */
@Serializable
data class PersonDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("biography") val biography: String = "",
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("deathday") val deathday: String? = null,
    @SerialName("place_of_birth") val placeOfBirth: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    @SerialName("popularity") val popularity: Double = 0.0,
    @SerialName("movie_credits") val movieCredits: PersonMovieCreditsDto = PersonMovieCreditsDto(),
)

@Serializable
data class PersonMovieCreditsDto(
    @SerialName("cast") val cast: List<PersonKnownForMovieDto> = emptyList(),
)

@Serializable
data class PersonKnownForMovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("popularity") val popularity: Double = 0.0,
)

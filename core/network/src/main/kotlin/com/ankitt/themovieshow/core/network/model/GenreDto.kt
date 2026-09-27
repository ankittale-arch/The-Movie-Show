package com.ankitt.themovieshow.core.network.model

import kotlinx.serialization.*

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)

@Serializable
data class GenreListDto(
    @SerialName("genres") val genres: List<GenreDto>,
)

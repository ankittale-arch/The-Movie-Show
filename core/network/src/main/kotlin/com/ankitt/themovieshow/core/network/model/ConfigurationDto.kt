package com.ankitt.themovieshow.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of GET /configuration. TMDB image URLs are relative paths (e.g. "/abc123.jpg"); the
 * client is expected to prefix them with a base URL and a size bucket obtained from here. We
 * fetch and cache this once (see [com.ankitt.themovieshow.core.network.TmdbApiService]) rather
 * than hardcoding image sizes, because TMDB documents this endpoint as the source of truth for
 * which size buckets currently exist.
 */
@Serializable
data class ConfigurationDto(
    @SerialName("images") val images: ImagesConfigurationDto,
)

@Serializable
data class ImagesConfigurationDto(
    @SerialName("secure_base_url") val secureBaseUrl: String,
    @SerialName("poster_sizes") val posterSizes: List<String>,
    @SerialName("backdrop_sizes") val backdropSizes: List<String>,
    @SerialName("profile_sizes") val profileSizes: List<String>,
)

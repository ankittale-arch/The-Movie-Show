package com.ankitt.themovieshow.core.data

import com.ankitt.themovieshow.core.network.BuildConfig

/**
 * Builds full TMDB image URLs from the relative paths stored on [com.ankitt.themovieshow.core.data.model.Movie]
 * (e.g. "/abc123.jpg"). Exposed from `:core:data` (rather than `:core:network` directly) because
 * `:feature:home` depends on `:core:data`, not `:core:network` — the UI layer must not reach past
 * the repository to Retrofit-adjacent types, per the repository-free-UI rule.
 */
object TmdbImageUrl {

    fun poster(path: String?, size: String = "w500"): String? =
        path?.let { "${BuildConfig.TMDB_IMAGE_BASE_URL}$size$it" }

    fun backdrop(path: String?, size: String = "w1280"): String? =
        path?.let { "${BuildConfig.TMDB_IMAGE_BASE_URL}$size$it" }
}

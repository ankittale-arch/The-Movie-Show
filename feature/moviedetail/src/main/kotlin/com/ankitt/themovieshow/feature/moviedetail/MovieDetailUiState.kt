package com.ankitt.themovieshow.feature.moviedetail

data class MovieDetailUiState(
    val movie: MovieDetailUi? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,
    val isOffline: Boolean = false,
    val isStale: Boolean = false,
    val lastSyncedAtEpochMillis: Long? = null,
)

data class MovieDetailUi(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val durationText: String?,
    val rating: Double,
    val language: String?,
    val tagline: String?,
    val genres: List<String>,
    val cast: List<CastMemberUi>,
    val trailerYoutubeKey: String?,
)

data class CastMemberUi(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
)

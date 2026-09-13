package com.ankitt.themovieshow.feature.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.common.network.ConnectivityObserver
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.MovieDetail
import com.ankitt.themovieshow.core.data.model.SyncMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val movieId = MutableStateFlow<Int?>(null)
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val detail = movieId.flatMapLatest { id ->
        if (id == null) flowOf(null) else movieRepository.observeMovieDetail(id)
    }

    private val isFavorite = movieId.flatMapLatest { id ->
        if (id == null) flowOf(false) else movieRepository.isFavorite(id)
    }

    private val isInWatchlist = movieId.flatMapLatest { id ->
        if (id == null) flowOf(false) else movieRepository.isInWatchlist(id)
    }

    private val syncMetadata = movieId.flatMapLatest { id ->
        if (id == null) flowOf(SyncMetadata(null, false, null)) else movieRepository.observeMovieDetailSyncMetadata(id)
    }

    // kotlinx.coroutines' typed `combine` overload tops out at 5 flows, and there are 7 total —
    // the first 5 are combined here, then combined again with sync/connectivity state below.
    private data class DetailCore(
        val detail: MovieDetail?,
        val isLoading: Boolean,
        val errorMessage: String?,
        val isFavorite: Boolean,
        val isInWatchlist: Boolean,
    )

    private val detailCore = combine(
        detail,
        isLoading,
        errorMessage,
        isFavorite,
        isInWatchlist,
    ) { detail, loading, error, favorite, inWatchlist ->
        DetailCore(detail, loading, error, favorite, inWatchlist)
    }

    val uiState: StateFlow<MovieDetailUiState> = combine(
        detailCore,
        syncMetadata,
        connectivityObserver.isOnline,
    ) { core, syncMetadata, isOnline ->
        MovieDetailUiState(
            movie = core.detail?.toUi(),
            isLoading = core.isLoading,
            errorMessage = core.errorMessage,
            isFavorite = core.isFavorite,
            isInWatchlist = core.isInWatchlist,
            isOffline = !isOnline,
            isStale = syncMetadata.isStale,
            lastSyncedAtEpochMillis = syncMetadata.lastSyncedAtEpochMillis,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = MovieDetailUiState(),
    )

    /** Idempotent per id: the screen calls this on every composition, but it only refreshes
     * from TMDB once per distinct [movieId] — repeat calls with the same id are no-ops. */
    fun load(movieId: Int) {
        if (this.movieId.value == movieId) return
        this.movieId.value = movieId
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = movieRepository.refreshMovieDetail(movieId)
            isLoading.value = false
            result.onFailure { errorMessage.value = it.message ?: "Couldn't load movie" }
        }
        viewModelScope.launch { movieRepository.recordMovieViewed(movieId) }
    }

    fun toggleFavorite() {
        val id = movieId.value ?: return
        viewModelScope.launch { movieRepository.toggleFavorite(id) }
    }

    fun toggleWatchlist() {
        val id = movieId.value ?: return
        viewModelScope.launch { movieRepository.toggleWatchlist(id) }
    }

    private fun MovieDetail.toUi() = MovieDetailUi(
        id = id,
        title = title,
        overview = overview,
        posterUrl = TmdbImageUrl.poster(posterPath),
        backdropUrl = TmdbImageUrl.backdrop(backdropPath),
        releaseDate = releaseDate,
        durationText = runtime?.let { "$it min." },
        rating = voteAverage,
        language = originalLanguage?.let { code -> displayLanguageOrNull(code) ?: code },
        tagline = tagline?.takeIf { it.isNotBlank() },
        genres = genres.map { it.name },
        trailerYoutubeKey = trailerYoutubeKey,
        cast = cast.map {
            CastMemberUi(
                id = it.id,
                name = it.name,
                character = it.character,
                profileUrl = TmdbImageUrl.profile(it.profilePath),
            )
        },
    )

    private fun displayLanguageOrNull(languageCode: String): String? = runCatching {
        Locale(languageCode).displayLanguage.replaceFirstChar { it.uppercase() }
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

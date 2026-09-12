package com.ankitt.themovieshow.feature.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.MovieDetail
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
) : ViewModel() {

    private val movieId = MutableStateFlow<Int?>(null)
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val detail = movieId.flatMapLatest { id ->
        if (id == null) flowOf(null) else movieRepository.observeMovieDetail(id)
    }

    val uiState: StateFlow<MovieDetailUiState> = combine(
        detail,
        isLoading,
        errorMessage,
    ) { detail, loading, error ->
        MovieDetailUiState(
            movie = detail?.toUi(),
            isLoading = loading,
            errorMessage = error,
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

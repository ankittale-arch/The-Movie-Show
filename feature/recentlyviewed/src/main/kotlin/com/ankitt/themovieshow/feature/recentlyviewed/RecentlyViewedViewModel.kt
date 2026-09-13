package com.ankitt.themovieshow.feature.recentlyviewed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyViewedViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    val uiState: StateFlow<RecentlyViewedUiState> = movieRepository.observeRecentlyViewedMovies()
        .map { movies -> RecentlyViewedUiState(movies = movies.map { it.toRecentlyViewedItem() }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = RecentlyViewedUiState(),
        )

    fun clearHistory() {
        viewModelScope.launch { movieRepository.clearRecentlyViewed() }
    }

    private fun Movie.toRecentlyViewedItem() = RecentlyViewedItem(
        id = id,
        title = title,
        posterUrl = TmdbImageUrl.poster(posterPath),
        voteAverage = voteAverage,
    )
}

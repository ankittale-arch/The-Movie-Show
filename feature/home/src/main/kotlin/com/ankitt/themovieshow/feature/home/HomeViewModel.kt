package com.ankitt.themovieshow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.common.network.ConnectivityObserver
import com.ankitt.themovieshow.core.data.HomeListKeys
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Depends only on [MovieRepository] (never on `core:network`'s Retrofit types or `core:database`'s
 * DAOs directly), per the repository-free-UI rule established by the Phase 1 placeholder
 * ViewModel. Each row is a Room-backed [kotlinx.coroutines.flow.Flow]; [refreshHome] is triggered
 * once on init to populate Room from TMDB the first time the screen is shown.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        moviesCombined(),
        movieRepository.observeGenres(),
        isRefreshing,
        movieRepository.observeHomeSyncMetadata(),
        connectivityObserver.isOnline,
    ) { movies, genres, refreshing, syncMetadata, isOnline ->
        HomeUiState(
            heroMovies = movies.hero.map { it.toHomeMovie() },
            genres = genres.map { HomeGenre(id = it.id, name = it.name) },
            nowPlaying = movies.nowPlaying.map { it.toHomeMovie() },
            popular = movies.popular.map { it.toHomeMovie() },
            discover = movies.discover.map { it.toHomeMovie() },
            upcoming = movies.upcoming.map { it.toHomeMovie() },
            isRefreshing = refreshing,
            isOffline = !isOnline,
            isStale = syncMetadata.isStale,
            lastSyncedAtEpochMillis = syncMetadata.lastSyncedAtEpochMillis,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch { movieRepository.refreshHome() }
    }

    /** Pull-to-refresh: bypasses [com.ankitt.themovieshow.core.data.CachePolicy] unconditionally. */
    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            movieRepository.refreshHome(forceRefresh = true)
            isRefreshing.value = false
        }
    }

    // kotlinx.coroutines' typed `combine` overload tops out at 5 flows, and there are 6 rows
    // total (5 movie lists + genres) — the 5 movie-list flows are combined here first, then
    // combined again with the genres flow above.
    private fun moviesCombined() = combine(
        movieRepository.observeMoviesForList(HomeListKeys.TRENDING),
        movieRepository.observeMoviesForList(HomeListKeys.NOW_PLAYING),
        movieRepository.observeMoviesForList(HomeListKeys.POPULAR),
        movieRepository.observeMoviesForList(HomeListKeys.DISCOVER),
        movieRepository.observeMoviesForList(HomeListKeys.UPCOMING),
    ) { hero, nowPlaying, popular, discover, upcoming ->
        HomeMovieLists(hero, nowPlaying, popular, discover, upcoming)
    }

    private data class HomeMovieLists(
        val hero: List<Movie>,
        val nowPlaying: List<Movie>,
        val popular: List<Movie>,
        val discover: List<Movie>,
        val upcoming: List<Movie>,
    )

    private fun Movie.toHomeMovie() = HomeMovie(
        id = id,
        title = title,
        posterUrl = TmdbImageUrl.poster(posterPath),
        backdropUrl = TmdbImageUrl.backdrop(backdropPath),
        voteAverage = voteAverage,
    )
}

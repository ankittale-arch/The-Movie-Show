package com.ankitt.themovieshow.feature.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Generic "full list" screen backing both a Home row's "more" page and a genre's movie list —
 * both are just `MovieRepository.observeMoviesForList(listKey)` plus paging through
 * `refreshMoviesForList`/`loadMoreMoviesForList`, so one ViewModel/screen serves either,
 * parameterized by whatever [HomeListKeys][com.ankitt.themovieshow.core.data.HomeListKeys] value
 * the caller passes.
 *
 * Page/hasMore state is kept here (not in Room): it's a transient property of this browsing
 * session, not cached data — TMDB's page 1 is still the only page Room needs to show instantly
 * next time, so nothing about offline-first breaks by not persisting it.
 */
@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val listKey = MutableStateFlow<String?>(null)
    private val isLoading = MutableStateFlow(false)
    private val isLoadingMore = MutableStateFlow(false)
    private val hasMorePages = MutableStateFlow(true)
    private val errorMessage = MutableStateFlow<String?>(null)

    private var currentPage = 1

    private val movies = listKey.flatMapLatest { key ->
        if (key == null) flowOf(emptyList()) else movieRepository.observeMoviesForList(key)
    }

    val uiState: StateFlow<MovieListUiState> = combine(
        movies,
        isLoading,
        isLoadingMore,
        hasMorePages,
        errorMessage,
    ) { movies, loading, loadingMore, hasMore, error ->
        MovieListUiState(
            movies = movies.map { it.toMovieListItem() },
            isLoading = loading,
            isLoadingMore = loadingMore,
            hasMorePages = hasMore,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = MovieListUiState(),
    )

    /** Idempotent per key: the screen calls this on every composition, but it only refreshes
     * from TMDB once per distinct [listKey] — repeat calls with the same key are no-ops. */
    fun load(listKey: String) {
        if (this.listKey.value == listKey) return
        this.listKey.value = listKey
        currentPage = 1
        hasMorePages.value = true
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val result = movieRepository.refreshMoviesForList(listKey)
            isLoading.value = false
            result.onFailure { errorMessage.value = it.message ?: "Couldn't load movies" }
        }
    }

    /** Called when the grid scrolls near its end. No-ops while a page is already in flight, the
     * initial load hasn't finished, or TMDB has reported no pages remain. */
    fun loadMore() {
        val key = listKey.value ?: return
        if (isLoading.value || isLoadingMore.value || !hasMorePages.value) return
        viewModelScope.launch {
            isLoadingMore.value = true
            val result = movieRepository.loadMoreMoviesForList(key, currentPage + 1)
            isLoadingMore.value = false
            result.fold(
                onSuccess = { moreRemain ->
                    currentPage += 1
                    hasMorePages.value = moreRemain
                },
                onFailure = { errorMessage.value = it.message ?: "Couldn't load more movies" },
            )
        }
    }

    private fun Movie.toMovieListItem() = MovieListItem(
        id = id,
        title = title,
        posterUrl = TmdbImageUrl.poster(posterPath),
    )
}

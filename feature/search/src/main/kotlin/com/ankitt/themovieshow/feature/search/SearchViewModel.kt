package com.ankitt.themovieshow.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.data.*
import com.ankitt.themovieshow.core.data.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEBOUNCE_MILLIS = 350L
private const val MIN_QUERY_LENGTH = 2

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val isSearching = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        movieRepository.observeMoviesForList(HomeListKeys.SEARCH),
        isSearching,
        errorMessage,
    ) { query, movies, isSearching, errorMessage ->
        SearchUiState(
            query = query,
            results = movies.map { it.toSearchResultMovie() },
            isSearching = isSearching,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SearchUiState(),
    )

    init {
        // Kept separate from `uiState` above on purpose: this is the network side-effect path.
        // `debounce` collapses a burst of keystrokes into one emission; `distinctUntilChanged`
        // skips re-firing on a semantically unchanged (e.g. re-trimmed) query; `collectLatest`
        // cancels whatever `searchMovies` call is still in flight — cancelling the coroutine
        // cancels the underlying Retrofit/OkHttp call too — the instant a newer query arrives, so
        // a fast typist never queues up more than one real network request.
        viewModelScope.launch {
            query
                .debounce(DEBOUNCE_MILLIS)
                .map { it.trim() }
                .distinctUntilChanged()
                .collectLatest { trimmed ->
                    if (trimmed.length < MIN_QUERY_LENGTH) {
                        errorMessage.value = null
                        isSearching.value = false
                        movieRepository.clearSearchResults()
                        return@collectLatest
                    }
                    isSearching.value = true
                    errorMessage.value = null
                    val result = movieRepository.searchMovies(trimmed)
                    isSearching.value = false
                    result.onFailure { errorMessage.value = it.message ?: "Search failed" }
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    private fun Movie.toSearchResultMovie() = SearchResultMovie(
        id = id,
        title = title,
        posterUrl = TmdbImageUrl.poster(posterPath),
        voteAverage = voteAverage,
    )
}

package com.ankitt.themovieshow.feature.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.TmdbImageUrl
import com.ankitt.themovieshow.core.data.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    val uiState: StateFlow<BookmarksUiState> = combine(
        movieRepository.observeFavoriteMovies(),
        movieRepository.observeWatchlistMovies(),
    ) { favorites, watchlist ->
        BookmarksUiState(
            favorites = favorites.map { it.toBookmarkItem() },
            watchlist = watchlist.map { it.toBookmarkItem() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = BookmarksUiState(),
    )

    fun removeFavorite(movieId: Int) {
        viewModelScope.launch { movieRepository.toggleFavorite(movieId) }
    }

    fun removeFromWatchlist(movieId: Int) {
        viewModelScope.launch { movieRepository.toggleWatchlist(movieId) }
    }

    private fun Movie.toBookmarkItem() = BookmarkItem(
        id = id,
        title = title,
        posterUrl = TmdbImageUrl.poster(posterPath),
        voteAverage = voteAverage,
    )
}

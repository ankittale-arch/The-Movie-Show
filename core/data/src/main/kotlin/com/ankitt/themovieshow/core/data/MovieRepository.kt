package com.ankitt.themovieshow.core.data

import com.ankitt.themovieshow.core.data.model.Genre
import com.ankitt.themovieshow.core.data.model.Movie
import com.ankitt.themovieshow.core.data.model.MovieDetail
import kotlinx.coroutines.flow.Flow

/**
 * The only thing `:feature:home`'s ViewModel is allowed to depend on for movie data — it never
 * sees [com.ankitt.themovieshow.core.network.api.TmdbApiService] or Room DAOs directly. Reads are
 * Room-backed [Flow]s (Room is the single source of truth); [refreshHome] is the one write path,
 * populating Room from TMDB.
 */
interface MovieRepository {

    fun observeMoviesForList(listKey: String): Flow<List<Movie>>

    fun observeGenres(): Flow<List<Genre>>

    /**
     * Fetches every Home row from TMDB and writes it into Room. Each list is refreshed
     * independently so one endpoint failing (e.g. a flaky `discover/movie` call) doesn't blank
     * out the rows that succeeded. Returns failure only if every list failed.
     */
    suspend fun refreshHome(): Result<Unit>

    /** Searches TMDB and replaces [HomeListKeys.SEARCH]'s cached row with the results. */
    suspend fun searchMovies(query: String): Result<Unit>

    /** Clears the cached search row, e.g. when the user clears the search box. */
    suspend fun clearSearchResults()

    /**
     * Fetches one row from TMDB and replaces its cached membership — the single-list building
     * block [refreshHome] runs five of concurrently. Accepts any [HomeListKeys] value (including
     * a `genre(id)` key) and dispatches to the matching TMDB call; an unrecognized key fails
     * rather than silently no-op-ing.
     */
    suspend fun refreshMoviesForList(listKey: String): Result<Unit>

    /**
     * Fetches [nextPage] for a row and appends it after the page(s) already cached (page 1 must
     * already be loaded via [refreshMoviesForList] first). On success, the [Result] carries
     * whether TMDB reports more pages beyond [nextPage] — the caller (a scroll-triggered "load
     * more") uses that to stop requesting once the list is exhausted.
     */
    suspend fun loadMoreMoviesForList(listKey: String, nextPage: Int): Result<Boolean>

    /** Null if [movieId] has never been cached (not in any list and never viewed before). */
    fun observeMovieDetail(movieId: Int): Flow<MovieDetail?>

    /** Fetches a movie's full detail + cast from TMDB and writes it into Room. */
    suspend fun refreshMovieDetail(movieId: Int): Result<Unit>
}

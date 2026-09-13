package com.ankitt.themovieshow.core.database.movie

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM movie_list_membership WHERE listKey = :listKey")
    suspend fun clearListMembership(listKey: String)

    @Upsert
    suspend fun upsertListMembership(entries: List<MovieListEntity>)

    /**
     * Replaces a row's membership atomically: upserts the movies themselves, drops the row's
     * prior membership entries, then inserts the new ones — so observers of
     * [observeMoviesForList] see one consistent update instead of a moment with no movies in the
     * row (which a delete-then-insert without a transaction would otherwise expose).
     */
    @Transaction
    suspend fun replaceListMembership(
        listKey: String,
        movies: List<MovieEntity>,
        memberships: List<MovieListEntity>,
    ) {
        upsertMovies(movies)
        clearListMembership(listKey)
        upsertListMembership(memberships)
    }

    /**
     * Appends a page onto a row's existing membership instead of replacing it — used for
     * "load more on scroll" (page 2+), where [replaceListMembership]'s clear-then-insert would
     * wipe out the pages already loaded.
     */
    @Transaction
    suspend fun appendListMembership(movies: List<MovieEntity>, memberships: List<MovieListEntity>) {
        upsertMovies(movies)
        upsertListMembership(memberships)
    }

    @Query("SELECT COUNT(*) FROM movie_list_membership WHERE listKey = :listKey")
    suspend fun countListMembership(listKey: String): Int

    @Query(
        """SELECT movie.* FROM movie
           INNER JOIN movie_list_membership ON movie.movieId = movie_list_membership.movieId
           WHERE movie_list_membership.listKey = :listKey
           ORDER BY movie_list_membership.position ASC""",
    )
    fun observeMoviesForList(listKey: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM genre ORDER BY name ASC")
    fun observeGenres(): Flow<List<GenreEntity>>
}

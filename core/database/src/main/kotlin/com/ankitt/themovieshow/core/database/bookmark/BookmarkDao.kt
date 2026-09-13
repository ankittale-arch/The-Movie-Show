package com.ankitt.themovieshow.core.database.bookmark

import androidx.room.*
import com.ankitt.themovieshow.core.database.movie.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Upsert
    suspend fun upsertFavorite(entity: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movie WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movie WHERE movieId = :movieId)")
    fun observeIsFavorite(movieId: Int): Flow<Boolean>

    /** Non-Flow snapshot, safe to call inside a `withTransaction` block (unlike [observeIsFavorite]). */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movie WHERE movieId = :movieId)")
    suspend fun isFavoriteOnce(movieId: Int): Boolean

    @Query(
        """SELECT movie.* FROM movie
           INNER JOIN favorite_movie ON movie.movieId = favorite_movie.movieId
           ORDER BY favorite_movie.addedAtEpochMillis DESC""",
    )
    fun observeFavoriteMovies(): Flow<List<MovieEntity>>

    @Upsert
    suspend fun upsertWatchlist(entity: WatchlistMovieEntity)

    @Query("DELETE FROM watchlist_movie WHERE movieId = :movieId")
    suspend fun deleteWatchlist(movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movie WHERE movieId = :movieId)")
    fun observeIsInWatchlist(movieId: Int): Flow<Boolean>

    /** Non-Flow snapshot, safe to call inside a `withTransaction` block (unlike [observeIsInWatchlist]). */
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movie WHERE movieId = :movieId)")
    suspend fun isInWatchlistOnce(movieId: Int): Boolean

    @Query(
        """SELECT movie.* FROM movie
           INNER JOIN watchlist_movie ON movie.movieId = watchlist_movie.movieId
           ORDER BY watchlist_movie.addedAtEpochMillis DESC""",
    )
    fun observeWatchlistMovies(): Flow<List<MovieEntity>>
}

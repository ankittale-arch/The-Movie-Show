package com.ankitt.themovieshow.core.database.recentlyviewed

import androidx.room.*
import com.ankitt.themovieshow.core.database.movie.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyViewedDao {

    @Upsert
    suspend fun upsertRecentlyViewed(entity: RecentlyViewedEntity)

    @Query(
        """SELECT movie.* FROM movie
           INNER JOIN recently_viewed ON movie.movieId = recently_viewed.movieId
           ORDER BY recently_viewed.viewedAtEpochMillis DESC
           LIMIT :limit""",
    )
    fun observeRecentlyViewedMovies(limit: Int): Flow<List<MovieEntity>>

    /** Keeps only the [limit] most recently viewed rows — called right after every upsert. */
    @Query(
        """DELETE FROM recently_viewed WHERE movieId NOT IN (
           SELECT movieId FROM recently_viewed ORDER BY viewedAtEpochMillis DESC LIMIT :limit)""",
    )
    suspend fun trimTo(limit: Int)

    @Query("DELETE FROM recently_viewed")
    suspend fun clearRecentlyViewed()
}

package com.ankitt.themovieshow.core.database.recentlyviewed

import androidx.room.*
import com.ankitt.themovieshow.core.database.movie.MovieEntity

@Entity(
    tableName = "recently_viewed",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RecentlyViewedEntity(
    @PrimaryKey val movieId: Int,
    val viewedAtEpochMillis: Long,
)

package com.ankitt.themovieshow.core.database.bookmark

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ankitt.themovieshow.core.database.movie.MovieEntity

@Entity(
    tableName = "watchlist_movie",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class WatchlistMovieEntity(
    @PrimaryKey val movieId: Int,
    val addedAtEpochMillis: Long,
)

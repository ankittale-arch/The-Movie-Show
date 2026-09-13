package com.ankitt.themovieshow.core.database.movie

import androidx.room.*

/**
 * Denormalized on purpose: no separate `Person` table, since nothing else in the app needs an
 * actor-detail screen yet — the same person appearing in two movies just gets two rows.
 */
@Entity(
    tableName = "movie_cast",
    primaryKeys = ["movieId", "castId"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["movieId", "order"])],
)
data class MovieCastEntity(
    val movieId: Int,
    val castId: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
    val order: Int,
)

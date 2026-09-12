package com.ankitt.themovieshow.core.database.movie

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 1:1 extension of [MovieEntity] for fields only `GET movie/{id}` returns (never a list
 * endpoint) — kept out of [MovieEntity] so every row populated by a list refresh doesn't carry
 * columns that stay null until the user opens that movie's detail screen.
 */
@Entity(
    tableName = "movie_detail",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MovieDetailEntity(
    @PrimaryKey val movieId: Int,
    val runtime: Int?,
    val tagline: String?,
    val originalLanguage: String?,
)

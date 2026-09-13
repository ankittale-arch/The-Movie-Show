package com.ankitt.themovieshow.core.database.movie

import androidx.room.*

/**
 * Which genres a movie has, per its detail response. Deliberately not modeled until now — Home's
 * Genres row never needed a movie↔genre link (it's a flat, unrelated chip list), so this table
 * would have been invented ahead of its use before the detail screen existed to read it.
 */
@Entity(
    tableName = "movie_genre_cross_ref",
    primaryKeys = ["movieId", "genreId"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GenreEntity::class,
            parentColumns = ["genreId"],
            childColumns = ["genreId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("genreId")],
)
data class MovieGenreCrossRef(
    val movieId: Int,
    val genreId: Int,
)

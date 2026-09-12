package com.ankitt.themovieshow.core.database.movie

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Membership of a [MovieEntity] in one of the Home screen's rows (Now Playing, Popular, Discover,
 * Upcoming, Trending), with that row's own display order. A movie can appear in more than one
 * row at once, and each row's order is independent of the others, so this is a many-to-many join
 * table keyed by `(listKey, movieId)` rather than an ordering column on [MovieEntity] itself.
 *
 * `listKey` values are plain strings (not an enum) so they line up 1:1 with the `resource` column
 * in `SyncStateEntity` (e.g. "now_playing", "popular") with no translation layer needed between
 * the two tables.
 */
@Entity(
    tableName = "movie_list_membership",
    primaryKeys = ["listKey", "movieId"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["movieId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("movieId"),
        Index(value = ["listKey", "position"]),
    ],
)
data class MovieListEntity(
    val listKey: String,
    val movieId: Int,
    val position: Int,
)

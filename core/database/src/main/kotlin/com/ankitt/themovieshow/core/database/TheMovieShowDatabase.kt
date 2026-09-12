package com.ankitt.themovieshow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ankitt.themovieshow.core.database.movie.GenreEntity
import com.ankitt.themovieshow.core.database.movie.MovieCastEntity
import com.ankitt.themovieshow.core.database.movie.MovieDao
import com.ankitt.themovieshow.core.database.movie.MovieDetailDao
import com.ankitt.themovieshow.core.database.movie.MovieDetailEntity
import com.ankitt.themovieshow.core.database.movie.MovieEntity
import com.ankitt.themovieshow.core.database.movie.MovieGenreCrossRef
import com.ankitt.themovieshow.core.database.movie.MovieListEntity
import com.ankitt.themovieshow.core.database.sync.SyncStateDao
import com.ankitt.themovieshow.core.database.sync.SyncStateEntity

/**
 * The single Room database for the app — the local source of truth described in the
 * architecture overview. [SyncStateEntity] was the only entity in Phase 1 (Room requires
 * `@Database.entities` to be non-empty, and sync bookkeeping is infrastructure rather than a
 * feature — see its own doc comment). [MovieEntity]/[GenreEntity]/[MovieListEntity] are added in
 * Phase 2 for the Home screen; CastEntity later, FavoriteEntity/WatchlistEntity/UserRatingEntity/
 * RecentlyViewedEntity in Phase 7, PendingOperationEntity in Phase 8 — each bumping [version] by
 * exactly one and shipping a [androidx.room.migration.Migration] plus a schema JSON snapshot
 * under core/database/schemas/.
 *
 * There is only one `@Database` class for the whole app, not one per feature module: Room does
 * not support multiple open connections to independent per-feature databases that also need to
 * join across each other's tables (e.g. favorites joining movies), and a single connection is
 * also what makes cross-table transactions (an offline mutation plus its outbox row, see Phase 8)
 * atomic.
 */
@Database(
    entities = [
        SyncStateEntity::class,
        MovieEntity::class,
        GenreEntity::class,
        MovieListEntity::class,
        MovieDetailEntity::class,
        MovieGenreCrossRef::class,
        MovieCastEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class TheMovieShowDatabase : RoomDatabase() {
    abstract fun syncStateDao(): SyncStateDao
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
}

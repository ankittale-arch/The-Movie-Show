package com.ankitt.themovieshow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ankitt.themovieshow.core.database.bookmark.*
import com.ankitt.themovieshow.core.database.movie.*
import com.ankitt.themovieshow.core.database.outbox.PendingOperationDao
import com.ankitt.themovieshow.core.database.outbox.PendingOperationEntity
import com.ankitt.themovieshow.core.database.recentlyviewed.RecentlyViewedDao
import com.ankitt.themovieshow.core.database.recentlyviewed.RecentlyViewedEntity
import com.ankitt.themovieshow.core.database.sync.SyncStateDao
import com.ankitt.themovieshow.core.database.sync.SyncStateEntity

/**
 * The single Room database for the app — the local source of truth described in the
 * architecture overview. [SyncStateEntity] was the only entity in Phase 1 (Room requires
 * `@Database.entities` to be non-empty, and sync bookkeeping is infrastructure rather than a
 * feature — see its own doc comment). [MovieEntity]/[GenreEntity]/[MovieListEntity] are added in
 * Phase 2 for the Home screen; CastEntity later, FavoriteEntity/WatchlistEntity in Phase 6,
 * [RecentlyViewedEntity] in Phase 7, [PendingOperationEntity] in Phase 8 — each bumping [version]
 * by exactly one and shipping a [androidx.room.migration.Migration] plus a schema JSON snapshot
 * under core/database/schemas/.
 *
 * There is only one `@Database` class for the whole app, not one per feature module: Room does
 * not support multiple open connections to independent per-feature databases that also need to
 * join across each other's tables (e.g. favorites joining movies), and a single connection is
 * also what makes cross-table transactions (an offline mutation plus its outbox row, see
 * [PendingOperationEntity]) atomic.
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
        FavoriteMovieEntity::class,
        WatchlistMovieEntity::class,
        RecentlyViewedEntity::class,
        PendingOperationEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class TheMovieShowDatabase : RoomDatabase() {
    abstract fun syncStateDao(): SyncStateDao
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun recentlyViewedDao(): RecentlyViewedDao
    abstract fun pendingOperationDao(): PendingOperationDao
}

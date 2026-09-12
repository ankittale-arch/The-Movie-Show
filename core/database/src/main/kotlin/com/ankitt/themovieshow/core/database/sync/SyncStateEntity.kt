package com.ankitt.themovieshow.core.database.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Last-successful-sync bookkeeping, one row per syncable resource (e.g. "trending", "popular",
 * "configuration" — a movie list, not an individual movie). This is what every screen's cache
 * policy in Phase 3 reads to decide "is this stale enough to trigger a background refresh", and
 * what the offline UI in Phase 3 reads to render "Last updated 12 minutes ago".
 *
 * This is the one entity created in Phase 1 rather than alongside its owning feature: Room
 * requires `@Database.entities` to be non-empty, and unlike every other entity in this schema
 * (MovieEntity, FavoriteEntity, ...), sync state is not owned by any single feature — it is
 * `core:database` infrastructure that Phase 6's sync engine and every feature's cache policy both
 * depend on, so it belongs here rather than being invented ahead of its use.
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val resource: String,
    val lastSyncedAtEpochMillis: Long,
    val isSyncing: Boolean,
    val lastErrorMessage: String?,
)

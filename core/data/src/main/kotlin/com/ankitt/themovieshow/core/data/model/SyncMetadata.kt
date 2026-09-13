package com.ankitt.themovieshow.core.data.model

/**
 * What a screen needs to render "Last updated 12 minutes ago" / "Stale, pull to refresh" offline
 * UX — derived from [com.ankitt.themovieshow.core.database.sync.SyncStateEntity] and
 * [com.ankitt.themovieshow.core.data.CachePolicy] rather than exposing either directly, so the UI
 * layer never has to know about Room entities or staleness thresholds.
 */
data class SyncMetadata(
    val lastSyncedAtEpochMillis: Long?,
    val isStale: Boolean,
    val lastErrorMessage: String?,
)

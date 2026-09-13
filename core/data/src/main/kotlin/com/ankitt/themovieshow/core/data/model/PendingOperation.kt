package com.ankitt.themovieshow.core.data.model

/**
 * One queued local mutation waiting to reach the server — read by Phase 6's WorkManager sync
 * worker, which replays each row against the backend and deletes it on success (or records a
 * failed attempt via [com.ankitt.themovieshow.core.data.MovieRepository.markPendingOperationFailed]
 * to retry later).
 */
data class PendingOperation(
    val id: Long,
    val operationType: String,
    val payload: String,
    val createdAtEpochMillis: Long,
    val retryCount: Int,
)

/** [PendingOperation.operationType] values this app currently enqueues. */
object PendingOperationType {
    const val ADD_FAVORITE = "add_favorite"
    const val REMOVE_FAVORITE = "remove_favorite"
    const val ADD_WATCHLIST = "add_watchlist"
    const val REMOVE_WATCHLIST = "remove_watchlist"
}

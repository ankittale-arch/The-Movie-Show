package com.ankitt.themovieshow.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.model.PendingOperation
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Outbox rows are dropped rather than retried forever past this many failed attempts. */
private const val MAX_RETRY_COUNT = 5

/**
 * Drains [MovieRepository]'s outbox: every offline favorite/watchlist toggle queued a
 * [PendingOperation] row (see `MovieRepositoryImpl.toggleFavorite`/`toggleWatchlist`), and this
 * worker is what eventually replays each one against a server and clears it. Scheduled
 * periodically by [SyncScheduler] with a `NetworkType.CONNECTED` constraint, so it never even
 * starts while offline.
 *
 * [syncToBackend] is currently a no-op: favorites/watchlist/personal ratings are local-only today
 * — TMDB's API this app calls is read-only and there is no authenticated backend of our own yet.
 * The outbox, retry bookkeeping and this worker are the real, durable infrastructure; this is the
 * one seam a future backend call slots into without changing anything else here.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val movieRepository: MovieRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendingOperations = movieRepository.getPendingOperations()
        var anyFailed = false

        for (operation in pendingOperations) {
            if (operation.retryCount >= MAX_RETRY_COUNT) {
                movieRepository.markPendingOperationSynced(operation.id)
                continue
            }

            val syncResult = runCatching { syncToBackend(operation) }
            if (syncResult.isSuccess) {
                movieRepository.markPendingOperationSynced(operation.id)
            } else {
                anyFailed = true
                movieRepository.markPendingOperationFailed(operation.id, syncResult.exceptionOrNull()?.message)
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }

    /** See class doc — placeholder until there is a backend to replay [operation] against. */
    private suspend fun syncToBackend(operation: PendingOperation) {
    }
}

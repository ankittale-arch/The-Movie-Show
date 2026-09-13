package com.ankitt.themovieshow.core.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

private const val OUTBOX_SYNC_WORK_NAME = "outbox_periodic_sync"
private const val OUTBOX_SYNC_INTERVAL_MINUTES = 15L

private const val DATA_REFRESH_WORK_NAME = "data_refresh_periodic"
private const val DATA_REFRESH_INTERVAL_HOURS = 24L

/**
 * Schedules [SyncWorker] and [DataRefreshWorker] to run periodically. Called once from
 * [SyncInitializer] at process start.
 */
object SyncScheduler {

    fun schedulePeriodicSync(context: Context) {
        val constraints = networkConnectedConstraints()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(OUTBOX_SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        // KEEP: re-scheduling on every process start must not reset an already-running periodic
        // request's interval/backoff state.
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(OUTBOX_SYNC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scheduleDataRefresh(context: Context) {
        val constraints = networkConnectedConstraints()

        val request = PeriodicWorkRequestBuilder<DataRefreshWorker>(DATA_REFRESH_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(DATA_REFRESH_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun networkConnectedConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}

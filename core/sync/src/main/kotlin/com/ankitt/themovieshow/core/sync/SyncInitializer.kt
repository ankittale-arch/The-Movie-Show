package com.ankitt.themovieshow.core.sync

import android.content.Context
import androidx.startup.Initializer

/**
 * Schedules the periodic outbox sync at process start via androidx.startup, registered in this
 * module's manifest — kept out of `TheMovieShowApplication.onCreate` on purpose (see its doc
 * comment). Scheduling work doesn't need Hilt: it only builds a [androidx.work.WorkRequest]
 * referencing [SyncWorker] by class; Hilt injects the worker's own dependencies later, when
 * WorkManager actually constructs it via `HiltWorkerFactory`.
 */
class SyncInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        SyncScheduler.schedulePeriodicSync(context)
        SyncScheduler.scheduleDataRefresh(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

package com.ankitt.themovieshow.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ankitt.themovieshow.core.data.MovieRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Keeps Room's Home data warm in the background so a cold app open is more likely to already be
 * fresh, without the user ever waiting on a foreground network call. Runs `forceRefresh = false`
 * — same as a screen-open refresh — so this is not "refetch everything every 24 hours" but rather
 * "check every resource's own [com.ankitt.themovieshow.core.data.CachePolicy] threshold and only
 * refetch what has actually gone stale since the last check." Trending (3h threshold) will
 * realistically refetch on every run; Upcoming (24h) and Genres (7d) often won't.
 *
 * Deliberately a separate worker from [SyncWorker] (the outbox drain): losing a queued
 * favorite/watchlist mutation is a correctness bug that must retry aggressively, while missing one
 * periodic data refresh is a non-event — the next screen open just refreshes in the foreground
 * instead. Coupling them would make one's failure/backoff policy leak into the other for no
 * benefit.
 */
@HiltWorker
class DataRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val movieRepository: MovieRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = movieRepository.refreshHome(forceRefresh = false)
        return if (result.isSuccess) Result.success() else Result.retry()
    }
}

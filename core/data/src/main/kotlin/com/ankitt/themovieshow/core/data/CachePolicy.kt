package com.ankitt.themovieshow.core.data

import java.time.Duration

/**
 * How long each [HomeListKeys] resource stays fresh before a screen open (or the background
 * [com.ankitt.themovieshow.core.sync] worker) should refetch it. Thresholds are deliberately
 * different per resource rather than one global TTL:
 *  - Trending is recomputed by TMDB roughly daily and is the one row where "recent" is the whole
 *    point, so it gets the shortest leash.
 *  - Popular/Top Rated/Discover drift slowly; refetching them as often as Trending would just be
 *    wasted network and battery for a list whose order barely moves within a few hours.
 *  - Upcoming is a release calendar — it does not change within a day.
 *  - Genres are effectively static (TMDB adds one a year, if that).
 *  - A genre(id) row (dynamic key, not in [THRESHOLDS]) shares [DEFAULT_THRESHOLD] with Discover,
 *    since it's the same "discover" TMDB endpoint under a different genre filter.
 *
 * Deliberately *not* using TMDB's `/movie/changes` endpoints to compute staleness — that API is
 * shaped for catalog aggregators diffing the entire TMDB corpus, not a client caching five curated
 * lists of ~20 movies each; paging through it to find overlap with our local cache would cost more
 * calls than just refetching the list directly.
 */
object CachePolicy {

    private val THRESHOLDS = mapOf(
        HomeListKeys.TRENDING to Duration.ofHours(3),
        HomeListKeys.NOW_PLAYING to Duration.ofHours(6),
        HomeListKeys.POPULAR to Duration.ofHours(12),
        HomeListKeys.DISCOVER to Duration.ofHours(12),
        HomeListKeys.UPCOMING to Duration.ofHours(24),
        HomeListKeys.GENRES to Duration.ofDays(7),
    )

    /** Used for genre(id) rows and anything else not explicitly listed above. */
    private val DEFAULT_THRESHOLD = Duration.ofHours(12)

    /** Movie detail (title/overview/cast/runtime) is effectively immutable once released. */
    private val MOVIE_DETAIL_THRESHOLD = Duration.ofHours(24)

    fun isListStale(
        listKey: String,
        lastSyncedAtEpochMillis: Long?,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): Boolean = isStale(lastSyncedAtEpochMillis, THRESHOLDS[listKey] ?: DEFAULT_THRESHOLD, nowEpochMillis)

    fun isMovieDetailStale(
        lastSyncedAtEpochMillis: Long?,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): Boolean = isStale(lastSyncedAtEpochMillis, MOVIE_DETAIL_THRESHOLD, nowEpochMillis)

    private fun isStale(lastSyncedAtEpochMillis: Long?, threshold: Duration, nowEpochMillis: Long): Boolean {
        if (lastSyncedAtEpochMillis == null) return true
        return nowEpochMillis - lastSyncedAtEpochMillis >= threshold.toMillis()
    }
}

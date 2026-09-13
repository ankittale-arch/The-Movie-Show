package com.ankitt.themovieshow.core.designsystem.text

import kotlin.time.Duration.Companion.milliseconds

/**
 * "Last updated 12 minutes ago"-style text for the offline-freshness banners. Pure Kotlin (no
 * `Context`/`DateUtils`) so it stays usable from a `@Preview` and testable without Robolectric.
 */
fun relativeTimeText(epochMillis: Long, nowEpochMillis: Long = System.currentTimeMillis()): String {
    val elapsed = (nowEpochMillis - epochMillis).coerceAtLeast(0).milliseconds
    return when {
        elapsed.inWholeMinutes < 1 -> "just now"
        elapsed.inWholeMinutes < 60 -> "${elapsed.inWholeMinutes} min ago"
        elapsed.inWholeHours < 24 -> "${elapsed.inWholeHours} hr ago"
        else -> "${elapsed.inWholeDays} d ago"
    }
}

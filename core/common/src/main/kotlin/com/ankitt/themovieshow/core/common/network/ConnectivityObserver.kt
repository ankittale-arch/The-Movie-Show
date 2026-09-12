package com.ankitt.themovieshow.core.common.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Live device connectivity, shared across the app.
 *
 * This is deliberately *not* "is the internet reachable right now" (that would require a network
 * round trip). It reports whether the OS currently has a network it considers usable for internet
 * traffic (NET_CAPABILITY_VALIDATED). That is enough to drive:
 *  - the offline banner in the UI,
 *  - WorkManager's own NetworkType.CONNECTED constraint (Phase 6),
 *  - deciding whether a repository should attempt a network refresh at all before hitting Room.
 *
 * A single shared [StateFlow] instance (rather than each caller opening its own
 * ConnectivityManager.NetworkCallback) keeps registration cost to one listener for the whole
 * process.
 */
interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
    fun observe(): Flow<Boolean>
}

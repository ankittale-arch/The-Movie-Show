package com.ankitt.themovieshow.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ankitt.themovieshow.core.designsystem.text.relativeTimeText

/**
 * "Offline · showing data from 12 min ago" / "Last updated 12 min ago · pull to refresh" banner.
 * Renders nothing when the data is both online and fresh — there's nothing useful to tell the
 * user then, and a banner that's always present stops meaning anything.
 */
@Composable
fun SyncStatusBanner(
    isOffline: Boolean,
    isStale: Boolean,
    lastSyncedAtEpochMillis: Long?,
    modifier: Modifier = Modifier,
) {
    if (!isOffline && !isStale) return

    val relativeTime = lastSyncedAtEpochMillis?.let { relativeTimeText(it) }
    val text = if (isOffline) {
        "Offline" + (relativeTime?.let { " · showing data from $it" } ?: "")
    } else {
        "Last updated ${relativeTime ?: "a while ago"} · pull to refresh"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isOffline) Icons.Filled.CloudOff else Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

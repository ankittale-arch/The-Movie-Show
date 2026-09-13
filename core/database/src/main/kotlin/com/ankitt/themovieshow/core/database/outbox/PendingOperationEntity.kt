package com.ankitt.themovieshow.core.database.outbox

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One queued local mutation that still needs to reach the server — the outbox row that makes an
 * offline write durable across process death until Phase 6's WorkManager sync worker can flush
 * it. No foreign key to the movie it describes: [payload] is a self-contained record of the
 * mutation (e.g. "toggle favorite for movieId 42"), and the row must survive independently of
 * whatever local cache row prompted it.
 */
@Entity(tableName = "pending_operation")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationType: String,
    val payload: String,
    val createdAtEpochMillis: Long,
    val retryCount: Int = 0,
    val lastAttemptEpochMillis: Long? = null,
    val lastErrorMessage: String? = null,
)

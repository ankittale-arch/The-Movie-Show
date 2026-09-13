package com.ankitt.themovieshow.core.database.outbox

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    @Insert
    suspend fun enqueue(operation: PendingOperationEntity): Long

    @Query("SELECT * FROM pending_operation ORDER BY createdAtEpochMillis ASC")
    suspend fun getAllPending(): List<PendingOperationEntity>

    @Query("SELECT * FROM pending_operation ORDER BY createdAtEpochMillis ASC")
    fun observePendingOperations(): Flow<List<PendingOperationEntity>>

    @Query("DELETE FROM pending_operation WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """UPDATE pending_operation
           SET retryCount = retryCount + 1, lastAttemptEpochMillis = :attemptedAtEpochMillis, lastErrorMessage = :errorMessage
           WHERE id = :id""",
    )
    suspend fun recordFailedAttempt(id: Long, attemptedAtEpochMillis: Long, errorMessage: String?)

    @Query("DELETE FROM pending_operation")
    suspend fun clearAll()
}

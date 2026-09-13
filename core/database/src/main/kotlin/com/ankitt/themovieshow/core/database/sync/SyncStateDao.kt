package com.ankitt.themovieshow.core.database.sync

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE resource = :resource")
    fun observe(resource: String): Flow<SyncStateEntity?>

    /** One-shot read for a staleness check before a refresh — no need to stay subscribed for that. */
    @Query("SELECT * FROM sync_state WHERE resource = :resource")
    suspend fun get(resource: String): SyncStateEntity?

    @Upsert
    suspend fun upsert(syncState: SyncStateEntity)
}

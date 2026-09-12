package com.ankitt.themovieshow.core.database.sync

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE resource = :resource")
    fun observe(resource: String): Flow<SyncStateEntity?>

    @Upsert
    suspend fun upsert(syncState: SyncStateEntity)
}

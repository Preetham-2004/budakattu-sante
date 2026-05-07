package com.budakattu.sante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.budakattu.sante.data.local.entity.SyncQueueEntity
import com.budakattu.sante.domain.model.SyncStatus

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType AND status = :status ORDER BY createdAt ASC")
    suspend fun getByStatus(entityType: String, status: SyncStatus = SyncStatus.PENDING): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = :status, lastAttemptAt = :timestamp WHERE queueId = :queueId")
    suspend fun updateStatus(queueId: String, status: SyncStatus, timestamp: Long)
}

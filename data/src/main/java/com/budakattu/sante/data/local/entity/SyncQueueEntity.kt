package com.budakattu.sante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budakattu.sante.domain.model.SyncStatus
import java.util.UUID

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val queueId: String = UUID.randomUUID().toString(),
    val entityType: String,
    val entityId: String,
    val operation: String,
    val status: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
)

package com.budakattu.sante.domain.model

enum class SyncStatus {
    PENDING,
    SYNCED,
    CONFLICT,
}

data class SyncQueueItem(
    val queueId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val status: SyncStatus,
    val payload: String,
    val retryCount: Int,
    val createdAt: Long,
    val lastAttemptAt: Long?,
)

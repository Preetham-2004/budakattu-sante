package com.budakattu.sante.domain.repository

import com.budakattu.sante.domain.model.SyncQueueItem

interface SyncRepository {
    suspend fun getPendingItems(entityType: String): List<SyncQueueItem>
    suspend fun markSynced(queueId: String)
}

package com.budakattu.sante.data.repository

import com.budakattu.sante.data.local.dao.SyncQueueDao
import com.budakattu.sante.data.mapper.toDomain
import com.budakattu.sante.domain.model.SyncQueueItem
import com.budakattu.sante.domain.model.SyncStatus
import com.budakattu.sante.domain.repository.SyncRepository
import javax.inject.Inject

class RoomSyncRepository @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
) : SyncRepository {
    override suspend fun getPendingItems(entityType: String): List<SyncQueueItem> {
        return syncQueueDao.getByStatus(entityType = entityType).map { it.toDomain() }
    }

    override suspend fun markSynced(queueId: String) {
        syncQueueDao.updateStatus(
            queueId = queueId,
            status = SyncStatus.SYNCED,
            timestamp = System.currentTimeMillis(),
        )
    }
}

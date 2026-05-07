package com.budakattu.sante.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.budakattu.sante.data.local.db.BudakattuDatabase
import com.budakattu.sante.data.repository.RoomSyncRepository

class ProductSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val repository = RoomSyncRepository(BudakattuDatabase.getInstance(applicationContext).syncQueueDao())
            val pending = repository.getPendingItems(entityType = "PRODUCT")
            pending.forEach { queueItem ->
                repository.markSynced(queueItem.queueId)
            }
            Result.success()
        } catch (exception: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(workDataOf("error" to (exception.message ?: "Unknown sync error")))
            }
        }
    }
}

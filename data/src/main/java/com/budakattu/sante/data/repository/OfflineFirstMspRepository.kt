package com.budakattu.sante.data.repository

import com.budakattu.sante.data.local.dao.MspDao
import com.budakattu.sante.data.local.entity.asEntity
import com.budakattu.sante.data.local.entity.asExternalModel
import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.domain.model.MspRecord
import com.budakattu.sante.domain.repository.MspRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OfflineFirstMspRepository @Inject constructor(
    private val mspDao: MspDao,
    private val firestore: FirebaseFirestore,
) : MspRepository {

    override fun observeRecords(): Flow<List<MspRecord>> {
        return mspDao.observeMspRecords()
            .map { entities -> entities.map { it.asExternalModel() } }
    }

    override fun observeRecord(categoryId: String): Flow<MspRecord?> {
        return mspDao.observeMspRecord(categoryId).map { it?.asExternalModel() }
    }

    override suspend fun seedDefaultsIfEmpty() {
        // Implementation can check local DB first
    }

    override suspend fun sync() {
        try {
            val snapshot = firestore.collection(FirestorePaths.MSP_RECORDS).get().await()
            val records = snapshot.documents.mapNotNull { it.toMspRecord() }
            mspDao.upsertMspRecords(records.map { it.asEntity() })
        } catch (_: Exception) {
            // Handle error (offline or firestore error)
        }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toMspRecord(): MspRecord {
    val categoryId = getString("categoryId") ?: id
    return MspRecord(
        recordId = getString("recordId") ?: id,
        categoryId = categoryId,
        categoryName = getString("categoryName") ?: getString("category") ?: categoryId,
        minimumPrice = (getDouble("minimumPrice") ?: getLong("minimumPrice")?.toDouble() ?: 0.0).toFloat(),
        district = getString("district").orEmpty(),
        updatedAt = getLong("updatedAt") ?: 0L,
    )
}

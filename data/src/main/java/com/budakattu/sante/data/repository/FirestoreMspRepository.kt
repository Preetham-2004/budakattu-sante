package com.budakattu.sante.data.repository

import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.domain.model.MspRecord
import com.budakattu.sante.domain.repository.MspRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreMspRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : MspRepository {
    private val collection = firestore.collection(FirestorePaths.MSP_RECORDS)

    override fun observeRecords(): Flow<List<MspRecord>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val records = snapshot?.documents
                ?.asSequence()?.mapNotNull { it.toMspRecord() }?.toList()
                ?.sortedBy { it.categoryName }
                .orEmpty()
            trySend(records)
        }
        awaitClose { listener.remove() }
    }

    override fun observeRecord(categoryId: String): Flow<MspRecord?> = callbackFlow {
        val listener = collection.document(categoryId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toMspRecord())
        }
        awaitClose { listener.remove() }
    }

    override suspend fun seedDefaultsIfEmpty() {
        val snapshot = awaitTask { collection.get() }
        if (!snapshot.isEmpty) return

        val now = System.currentTimeMillis()
        val defaults = listOf(
            MspRecord("honey", "honey", "Honey", 320f, "BR Hills", now),
            MspRecord("bamboo-crafts", "bamboo-crafts", "Bamboo Crafts", 450f, "BR Hills", now),
            MspRecord("herbal-produce", "herbal-produce", "Herbal Produce", 180f, "BR Hills", now),
            MspRecord("others", "others", "Others", 120f, "BR Hills", now),
        )
        val batch = firestore.batch()
        defaults.forEach { record ->
            batch[collection.document(record.categoryId)] = record.toMap()
        }
        awaitTask { batch.commit() }
    }

    override suspend fun sync() {
        // Direct Firestore implementation doesn't need explicit sync
    }

    private suspend fun <T> awaitTask(block: () -> com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            block()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
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

private fun MspRecord.toMap(): Map<String, Any> = mapOf(
    "recordId" to recordId,
    "categoryId" to categoryId,
    "categoryName" to categoryName,
    "minimumPrice" to minimumPrice,
    "district" to district,
    "updatedAt" to updatedAt,
)

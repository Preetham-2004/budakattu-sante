package com.budakattu.sante.data.repository

import com.budakattu.sante.data.remote.firebase.BatchRecordDocument
import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.data.remote.firebase.SupplyLogDocument
import com.budakattu.sante.data.remote.firebase.toBatchRecordDomain
import com.budakattu.sante.data.remote.firebase.toDocument
import com.budakattu.sante.data.remote.firebase.toSupplyLogDomain
import com.budakattu.sante.data.remote.firebase.toTribalFamilyDomain
import com.budakattu.sante.domain.model.BatchRecord
import com.budakattu.sante.domain.model.SupplyLog
import com.budakattu.sante.domain.model.TribalFamily
import com.budakattu.sante.domain.repository.TraceabilityRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreTraceabilityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : TraceabilityRepository {
    private val families = firestore.collection(FirestorePaths.TRIBAL_FAMILIES)
    private val supplyLogs = firestore.collection(FirestorePaths.SUPPLY_LOGS)
    private val batches = firestore.collection(FirestorePaths.BATCH_RECORDS)

    override fun observeFamilies(cooperativeId: String): Flow<List<TribalFamily>> = callbackFlow {
        val listener = families.whereEqualTo("cooperativeId", cooperativeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { it.toTribalFamilyDomain() }
                    .orEmpty()
                    .sortedBy { it.familyName }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override fun observeFamily(familyId: String): Flow<TribalFamily?> = callbackFlow {
        val listener = families.document(familyId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.toTribalFamilyDomain())
        }
        awaitClose { listener.remove() }
    }

    override fun observeSupplyLogs(cooperativeId: String): Flow<List<SupplyLog>> = callbackFlow {
        val listener = supplyLogs.whereEqualTo("cooperativeId", cooperativeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { it.toSupplyLogDomain() }
                    .orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override fun observeSupplyLogsForProduct(productId: String): Flow<List<SupplyLog>> = callbackFlow {
        val listener = supplyLogs.whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { it.toSupplyLogDomain() }
                    .orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override fun observeBatchRecordsForProduct(productId: String): Flow<List<BatchRecord>> = callbackFlow {
        val listener = batches.whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { it.toBatchRecordDomain() }
                    .orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun upsertFamily(family: TribalFamily) {
        awaitTask {
            families.document(family.familyId).set(family.toDocument())
        }
    }

    override suspend fun createSupplyLog(
        cooperativeId: String,
        productId: String,
        productName: String,
        family: TribalFamily,
        quantity: Int,
        unit: String,
        harvestDate: String,
        originVillage: String,
        notes: String,
    ) {
        val now = System.currentTimeMillis()
        val batchId = buildBatchId(productName, now)
        val logId = UUID.randomUUID().toString()
        val log = SupplyLogDocument(
            logId = logId,
            cooperativeId = cooperativeId,
            productId = productId,
            productName = productName,
            familyId = family.familyId,
            familyName = family.familyName,
            batchId = batchId,
            quantity = quantity.toLong(),
            unit = unit,
            harvestDate = harvestDate,
            originVillage = originVillage,
            notes = notes,
            createdAt = now,
        )
        val batch = BatchRecordDocument(
            batchId = batchId,
            cooperativeId = cooperativeId,
            productId = productId,
            productName = productName,
            familyId = family.familyId,
            familyName = family.familyName,
            harvestDate = harvestDate,
            originVillage = originVillage,
            quantity = quantity.toLong(),
            unit = unit,
            status = "RECORDED",
            createdAt = now,
            updatedAt = now,
        )
        awaitTask {
            firestore.runBatch { batchWrite ->
                batchWrite.set(supplyLogs.document(logId), log)
                batchWrite.set(batches.document(batchId), batch)
            }
        }
    }

    private fun buildBatchId(productName: String, now: Long): String {
        val slug = productName.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "batch" }
        return "$slug-$now"
    }

    private suspend fun <T> awaitTask(block: () -> com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            block()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}

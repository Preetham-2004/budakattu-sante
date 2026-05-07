package com.budakattu.sante.data.repository

import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.data.remote.firebase.toFirestoreDocument
import com.budakattu.sante.data.remote.firebase.toProductDomain
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ProductRepository {
    private val collection = firestore.collection(FirestorePaths.COOPERATIVES)
        .document(FirestorePaths.DEFAULT_COOPERATIVE_ID)
        .collection(FirestorePaths.PRODUCTS)

    override fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val products = snapshot?.documents
                ?.mapNotNull { it.toProductDomain() }
                ?.sortedByDescending { it.addedAt }
                .orEmpty()
            trySend(products)
        }
        awaitClose { listener.remove() }
    }

    override fun getProduct(productId: String): Flow<Product?> = callbackFlow {
        val listener = collection.document(productId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toProductDomain())
        }
        awaitClose { listener.remove() }
    }

    override suspend fun seedProductsIfEmpty() {
        val snapshot = awaitTask { collection.get() }
        if (!snapshot.isEmpty) {
            return
        }

        val batch = firestore.batch()
        CatalogSeedData.products().forEach { product ->
            batch.set(collection.document(product.productId), product.toFirestoreDocument())
        }
        awaitTask { batch.commit() }
    }

    private suspend fun <T> awaitTask(block: () -> com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            block()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}

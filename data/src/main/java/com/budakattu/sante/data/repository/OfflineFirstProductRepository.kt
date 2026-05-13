package com.budakattu.sante.data.repository

import com.budakattu.sante.data.local.dao.ProductDao
import com.budakattu.sante.data.local.entity.asEntity
import com.budakattu.sante.data.local.entity.asExternalModel
import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.data.remote.firebase.toFirestoreDocument
import com.budakattu.sante.data.remote.firebase.toProductDomain
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OfflineFirstProductRepository @Inject constructor(
    private val productDao: ProductDao,
    firestore: FirebaseFirestore,
) : ProductRepository {

    private val collection = firestore.collection(FirestorePaths.COOPERATIVES)
        .document(FirestorePaths.DEFAULT_COOPERATIVE_ID)
        .collection(FirestorePaths.PRODUCTS)

    override fun getProducts(includeDrafts: Boolean): Flow<List<Product>> {
        return productDao.observeProducts()
            .map { entities -> 
                entities.map { it.asExternalModel() }
                    .filter { includeDrafts || !it.isDraft }
            }
    }

    override fun getProduct(productId: String): Flow<Product?> {
        return productDao.observeProduct(productId).map { it?.asExternalModel() }
    }

    override suspend fun upsertProduct(product: Product) {
        // Optimistic update
        productDao.upsertProducts(listOf(product.asEntity()))
        try {
            collection.document(product.productId).set(product.toFirestoreDocument()).await()
        } catch (_: Exception) {
            // If failed, it's still in local DB, WorkManager could retry later
        }
    }

    override suspend fun deleteProduct(productId: String) {
        productDao.deleteProduct(productId)
        try {
            collection.document(productId).delete().await()
        } catch (_: Exception) {
            // Handle error
        }
    }

    override suspend fun seedProductsIfEmpty() {
        // Implementation
    }

    override suspend fun sync() {
        try {
            val snapshot = collection.get().await()
            val products = snapshot.documents.mapNotNull { it.toProductDomain() }
            productDao.upsertProducts(products.map { it.asEntity() })
        } catch (_: Exception) {
            // Offline
        }
    }
}

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
import javax.inject.Inject
import com.budakattu.sante.data.repository.CatalogSeedData
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

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
                entities.asSequence()
                    .map { it.asExternalModel() }
                    .filter { includeDrafts || !it.isDraft }
                    .toList()
            }
    }

    override fun getProduct(productId: String): Flow<Product?> {
        return productDao.observeProduct(productId).map { it?.asExternalModel() }
    }

    override suspend fun upsertProduct(product: Product) {
        // Optimistic update to local database
        productDao.upsertProducts(listOf(product.asEntity()))
        
        // Asynchronous update to Firestore - let the SDK handle offline persistence.
        // We don't await here to allow the UI to proceed while offline.
        collection.document(product.productId).set(product.toFirestoreDocument())
    }

    override suspend fun deleteProduct(productId: String) {
        // Optimistic delete from local database
        productDao.deleteProduct(productId)
        
        // Asynchronous delete from Firestore
        collection.document(productId).delete()
    }

    override suspend fun seedProductsIfEmpty() {
        val count = productDao.getProductCount()
        if (count == 0) {
            val seedProducts = CatalogSeedData.products()
            productDao.upsertProducts(seedProducts.map { it.asEntity() })
        }
    }

    override suspend fun sync() {
        try {
            val snapshot = collection.get().await()
            val products = snapshot.documents.mapNotNull { it.toProductDomain() }
            Log.d("ProductRepo", "Syncing ${products.size} products from Firestore")
            productDao.upsertProducts(products.map { it.asEntity() })
        } catch (e: Exception) {
            Log.e("ProductRepo", "Sync failed: ${e.message}")
        }
    }
}

package com.budakattu.sante.data.repository

import com.budakattu.sante.data.local.dao.ProductDao
import com.budakattu.sante.data.local.dao.SyncQueueDao
import com.budakattu.sante.data.local.entity.SyncQueueEntity
import com.budakattu.sante.data.mapper.toDomain
import com.budakattu.sante.data.mapper.toEntity
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val syncQueueDao: SyncQueueDao,
) : ProductRepository {
    override fun getProducts(): Flow<List<Product>> {
        return productDao.observeProducts().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getProduct(productId: String): Flow<Product?> {
        return productDao.observeProduct(productId).map { entity -> entity?.toDomain() }
    }

    override suspend fun seedProductsIfEmpty() {
        if (productDao.count() > 0) {
            return
        }

        val products = CatalogSeedData.products()
        productDao.insertAll(products.map { it.toEntity() })
        products.forEach { product ->
            syncQueueDao.insert(
                SyncQueueEntity(
                    entityType = "PRODUCT",
                    entityId = product.productId,
                    operation = "INSERT",
                    payload = "${product.name}|${product.pricePerUnit}|${product.stockQty}",
                ),
            )
        }
    }
}

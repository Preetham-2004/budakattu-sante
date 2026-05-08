package com.budakattu.sante.domain.repository

import com.budakattu.sante.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    fun getProduct(productId: String): Flow<Product?>
    suspend fun upsertProduct(product: Product)
    suspend fun seedProductsIfEmpty()
}

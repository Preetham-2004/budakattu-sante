package com.budakattu.sante.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.budakattu.sante.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY lastModifiedAt DESC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId")
    fun observeProduct(productId: String): Flow<ProductEntity?>

    @Upsert
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

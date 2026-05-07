package com.budakattu.sante.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index("categoryId"),
        Index("familyId"),
        Index("pendingSync"),
        Index("isSeasonal"),
    ],
)
data class ProductEntity(
    @PrimaryKey val productId: String,
    val familyId: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val categoryName: String,
    val familyName: String,
    val village: String,
    val pricePerUnit: Float,
    val mspPerUnit: Float,
    val unit: String,
    val stockQty: Int,
    val isSeasonal: Boolean,
    val season: String?,
    val imageUrls: List<String>,
    val isAvailable: Boolean,
    val addedAt: Long,
    val pendingSync: Boolean = false,
    val lastModifiedAt: Long = System.currentTimeMillis(),
)

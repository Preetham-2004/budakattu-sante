package com.budakattu.sante.domain.model

data class Product(
    val productId: String,
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
    val lastModifiedAt: Long,
)

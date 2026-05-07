package com.budakattu.sante.data.remote.firebase

data class ProductDocument(
    val productId: String = "",
    val familyId: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val categoryName: String = "",
    val familyName: String = "",
    val village: String = "",
    val pricePerUnit: Double = 0.0,
    val mspPerUnit: Double = 0.0,
    val unit: String = "kg",
    val stockQty: Long = 0,
    val isSeasonal: Boolean = false,
    val season: String? = null,
    val imageUrls: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val addedAt: Long = 0L,
    val lastModifiedAt: Long = 0L,
)

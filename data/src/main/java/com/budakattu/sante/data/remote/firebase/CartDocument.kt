package com.budakattu.sante.data.remote.firebase

data class CartDocument(
    val userId: String = "",
    val totalItems: Long = 0,
    val updatedAt: Long = 0L,
)

data class CartItemDocument(
    val itemId: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Long = 0,
    val pricePerUnit: Double = 0.0,
    val unit: String = "",
    val imageUrl: String? = null,
    val availability: String = "IN_STOCK",
    val expectedDispatchDate: String? = null,
    val familyName: String = "",
    val village: String = "",
)

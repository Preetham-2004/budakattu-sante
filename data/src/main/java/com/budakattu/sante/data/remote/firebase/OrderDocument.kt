package com.budakattu.sante.data.remote.firebase

data class OrderDocument(
    val orderId: String = "",
    val userId: String = "",
    val cooperativeId: String = "",
    val status: String = "PENDING",
    val orderType: String = "READY",
    val totalItems: Long = 0,
    val totalAmount: Double = 0.0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val expectedDispatchDate: String? = null,
)

data class OrderItemDocument(
    val itemId: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Long = 0,
    val pricePerUnit: Double = 0.0,
    val unit: String = "",
    val imageUrl: String? = null,
    val familyName: String = "",
    val village: String = "",
    val availability: String = "IN_STOCK",
    val batchId: String? = null,
    val expectedDispatchDate: String? = null,
)

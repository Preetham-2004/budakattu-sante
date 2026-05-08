package com.budakattu.sante.domain.model

enum class OrderStatus {
    PENDING,
    RESERVED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
}

enum class OrderType {
    READY,
    PREBOOK,
    MIXED,
}

data class Cart(
    val userId: String,
    val items: List<CartItem>,
    val totalItems: Int,
    val updatedAt: Long,
)

data class CartItem(
    val itemId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Float,
    val unit: String,
    val imageUrl: String?,
    val availability: ProductAvailability,
    val expectedDispatchDate: String?,
    val familyName: String,
    val village: String,
)

data class Order(
    val orderId: String,
    val userId: String,
    val cooperativeId: String,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val orderType: OrderType,
    val totalItems: Int,
    val totalAmount: Float,
    val createdAt: Long,
    val updatedAt: Long,
    val expectedDispatchDate: String?,
)

data class OrderItem(
    val itemId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Float,
    val unit: String,
    val imageUrl: String?,
    val familyName: String,
    val village: String,
    val availability: ProductAvailability,
    val batchId: String?,
    val expectedDispatchDate: String?,
)

data class CheckoutResult(
    val orderId: String,
    val status: OrderStatus,
    val orderType: OrderType,
)

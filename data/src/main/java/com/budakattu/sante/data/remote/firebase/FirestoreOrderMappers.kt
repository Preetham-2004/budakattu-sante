package com.budakattu.sante.data.remote.firebase

import com.budakattu.sante.domain.model.Cart
import com.budakattu.sante.domain.model.CartItem
import com.budakattu.sante.domain.model.Order
import com.budakattu.sante.domain.model.OrderItem
import com.budakattu.sante.domain.model.OrderStatus
import com.budakattu.sante.domain.model.OrderType
import com.budakattu.sante.domain.model.ProductAvailability
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot

fun QueryDocumentSnapshot.toCartItemDomain(): CartItem {
    val document = toObject(CartItemDocument::class.java)
    return CartItem(
        itemId = document.itemId.ifBlank { id },
        productId = document.productId,
        productName = document.productName,
        quantity = document.quantity.toInt(),
        pricePerUnit = document.pricePerUnit.toFloat(),
        unit = document.unit,
        imageUrl = document.imageUrl,
        availability = document.availability.toAvailability(),
        expectedDispatchDate = document.expectedDispatchDate,
        familyName = document.familyName,
        village = document.village,
    )
}

fun DocumentSnapshot.toOrderDomain(items: List<OrderItem>): Order? {
    val document = toObject(OrderDocument::class.java) ?: return null
    return Order(
        orderId = document.orderId.ifBlank { id },
        userId = document.userId,
        cooperativeId = document.cooperativeId,
        items = items,
        status = document.status.toOrderStatus(),
        orderType = document.orderType.toOrderType(),
        totalItems = document.totalItems.toInt(),
        totalAmount = document.totalAmount.toFloat(),
        createdAt = document.createdAt,
        updatedAt = document.updatedAt,
        expectedDispatchDate = document.expectedDispatchDate,
    )
}

fun QueryDocumentSnapshot.toOrderItemDomain(): OrderItem {
    val document = toObject(OrderItemDocument::class.java)
    return OrderItem(
        itemId = document.itemId.ifBlank { id },
        productId = document.productId,
        productName = document.productName,
        quantity = document.quantity.toInt(),
        pricePerUnit = document.pricePerUnit.toFloat(),
        unit = document.unit,
        imageUrl = document.imageUrl,
        familyName = document.familyName,
        village = document.village,
        availability = document.availability.toAvailability(),
        batchId = document.batchId,
        expectedDispatchDate = document.expectedDispatchDate,
    )
}

fun Cart.toCartDocument() = CartDocument(
    userId = userId,
    totalItems = totalItems.toLong(),
    updatedAt = updatedAt,
)

private fun String.toAvailability(): ProductAvailability {
    return ProductAvailability.entries.firstOrNull { it.name == this }
        ?: ProductAvailability.IN_STOCK
}

private fun String.toOrderStatus(): OrderStatus {
    return OrderStatus.entries.firstOrNull { it.name == this }
        ?: OrderStatus.PENDING
}

private fun String.toOrderType(): OrderType {
    return OrderType.entries.firstOrNull { it.name == this }
        ?: OrderType.READY
}

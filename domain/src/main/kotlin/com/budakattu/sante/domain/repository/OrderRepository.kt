package com.budakattu.sante.domain.repository

import com.budakattu.sante.domain.model.Cart
import com.budakattu.sante.domain.model.CheckoutResult
import com.budakattu.sante.domain.model.Order
import com.budakattu.sante.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeCart(userId: String): Flow<Cart?>
    suspend fun addToCart(userId: String, productId: String, quantity: Int)
    suspend fun updateCartItemQuantity(userId: String, itemId: String, quantity: Int)
    suspend fun removeCartItem(userId: String, itemId: String)
    suspend fun checkout(userId: String): CheckoutResult
    fun observeBuyerOrders(userId: String): Flow<List<Order>>
    fun observeOrder(orderId: String): Flow<Order?>
    fun observeLeaderOrders(cooperativeId: String): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
}

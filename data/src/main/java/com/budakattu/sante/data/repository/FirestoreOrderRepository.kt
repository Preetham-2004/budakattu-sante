package com.budakattu.sante.data.repository

import com.budakattu.sante.data.remote.firebase.CartDocument
import com.budakattu.sante.data.remote.firebase.CartItemDocument
import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.data.remote.firebase.OrderDocument
import com.budakattu.sante.data.remote.firebase.OrderItemDocument
import com.budakattu.sante.data.remote.firebase.ProductDocument
import com.budakattu.sante.data.remote.firebase.toCartItemDomain
import com.budakattu.sante.data.remote.firebase.toOrderDomain
import com.budakattu.sante.data.remote.firebase.toOrderItemDomain
import com.budakattu.sante.domain.model.Cart
import com.budakattu.sante.domain.model.CheckoutResult
import com.budakattu.sante.domain.model.Order
import com.budakattu.sante.domain.model.OrderItem
import com.budakattu.sante.domain.model.OrderStatus
import com.budakattu.sante.domain.model.OrderType
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.repository.OrderRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.util.Log

class FirestoreOrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : OrderRepository {
    private val carts: CollectionReference = firestore.collection(FirestorePaths.CARTS)
    private val orders: CollectionReference = firestore.collection(FirestorePaths.ORDERS)
    private val products: CollectionReference = firestore.collection(FirestorePaths.COOPERATIVES)
        .document(FirestorePaths.DEFAULT_COOPERATIVE_ID)
        .collection(FirestorePaths.PRODUCTS)

    override fun observeCart(userId: String): Flow<Cart?> = callbackFlow {
        val cartRef = carts.document(userId)
        val itemRef = cartRef.collection(FirestorePaths.ORDER_ITEMS)
        var latestCart: CartDocument? = null
        var latestItems: List<com.budakattu.sante.domain.model.CartItem> = emptyList()

        fun emitCart() {
            val cart = latestCart
            if ((cart == null) && latestItems.isEmpty()) {
                trySend(null)
            } else {
                trySend(
                    Cart(
                        userId = cart?.userId.orEmpty().ifBlank { userId },
                        items = latestItems,
                        totalItems = latestItems.sumOf { it.quantity },
                        updatedAt = cart?.updatedAt ?: 0L,
                    ),
                )
            }
        }

        val cartListener = cartRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    trySend(null)
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            latestCart = snapshot?.toObject(CartDocument::class.java)
            emitCart()
        }
        val itemListener = itemRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    trySend(null)
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            latestItems = snapshot?.documents
                ?.asSequence()?.mapNotNull { it as? QueryDocumentSnapshot }
                ?.map { it.toCartItemDomain() }
                ?.toList() ?: emptyList()
            emitCart()
        }

        awaitClose {
            cartListener.remove()
            itemListener.remove()
        }
    }

    override suspend fun addToCart(userId: String, productId: String, quantity: Int) {
        val productRef = products.document(productId)
        val cartRef = carts.document(userId)
        val itemRef = cartRef.collection(FirestorePaths.ORDER_ITEMS).document(productId)

        awaitTask {
            firestore.runTransaction { transaction ->
                val product = transaction[productRef].toObject(ProductDocument::class.java)
                    ?: throw IllegalStateException("Product not found")

                validateCartQuantity(product, quantity)

                val cartItem = transaction.get(itemRef).toObject(CartItemDocument::class.java)
                val newQuantity = (cartItem?.quantity ?: 0).toInt() + quantity

                if (cartItem == null) {
                    transaction[itemRef] = CartItemDocument(
                            itemId = productId,
                            productId = productId,
                            productName = product.name,
                            quantity = newQuantity.toLong(),
                            pricePerUnit = product.pricePerUnit,
                            unit = product.unit,
                            imageUrl = product.imageUrls.firstOrNull(),
                            familyName = product.familyName,
                            village = product.village,
                            availability = product.availability,
                            expectedDispatchDate = product.expectedDispatchDate,
                        )
                } else {
                    transaction.update(itemRef, "quantity", newQuantity.toLong())
                }

                transaction.set(
                    cartRef,
                    CartDocument(
                        userId = userId,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    override suspend fun updateCartItemQuantity(userId: String, itemId: String, quantity: Int) {
        val cartRef = carts.document(userId)
        val itemRef = cartRef.collection(FirestorePaths.ORDER_ITEMS).document(itemId)
        val productRef = products.document(itemId)

        awaitTask {
            firestore.runTransaction { transaction ->
                val product = transaction[productRef].toObject(ProductDocument::class.java)
                    ?: throw IllegalStateException("Product not found")

                validateCartQuantity(product, quantity)

                if (quantity <= 0) {
                    transaction.delete(itemRef)
                } else {
                    transaction.update(itemRef, "quantity", quantity.toLong())
                }

                transaction.update(cartRef, "updatedAt", System.currentTimeMillis())
            }
        }
    }

    override suspend fun removeCartItem(userId: String, itemId: String) {
        updateCartItemQuantity(userId, itemId, 0)
    }

    override suspend fun checkout(userId: String): CheckoutResult {
        val cartRef = carts.document(userId)
        val itemsRef = cartRef.collection(FirestorePaths.ORDER_ITEMS)

        val itemsSnapshot = awaitTask { itemsRef.get() }
        val items = itemsSnapshot.documents.mapNotNull { 
            it.toObject(CartItemDocument::class.java)
        }

        if (items.isEmpty()) throw IllegalStateException("Cart is empty")

        return awaitTask {
            firestore.runTransaction { transaction ->
                val now = System.currentTimeMillis()
                val orderId = orders.document().id
                var totalAmount = 0.0
                var totalItems = 0L

                items.forEach { item ->
                    val productRef = products.document(item.productId)
                    val product = transaction[productRef].toObject(ProductDocument::class.java)
                        ?: throw IllegalStateException("Product ${item.productName} not found")

                    validateCheckoutQuantity(product, item.quantity.toInt())

                    when (product.availability.toAvailability()) {
                        ProductAvailability.IN_STOCK -> {
                            transaction.update(
                                productRef,
                                mapOf(
                                    "availableStock" to (product.availableStock - item.quantity),
                                    "soldStock" to (product.soldStock + item.quantity),
                                    "lastModifiedAt" to now,
                                ),
                            )
                        }

                        ProductAvailability.PREBOOK_OPEN,
                        ProductAvailability.COMING_SOON,
                        -> {
                            transaction.update(
                                productRef,
                                mapOf(
                                    "reservedStock" to (product.reservedStock + item.quantity),
                                    "lastModifiedAt" to now,
                                ),
                            )
                        }

                        ProductAvailability.SOLD_OUT -> throw IllegalStateException("${product.name} is sold out")
                    }

                    totalAmount += item.quantity.toDouble() * item.pricePerUnit
                    totalItems += item.quantity

                    transaction.set(
                        orders.document(orderId).collection(FirestorePaths.ORDER_ITEMS).document(item.itemId),
                        item,
                    )

                    transaction.delete(itemsRef.document(item.itemId))
                }

                val orderSnapshot = items.first() 
                val availability = orderSnapshot.availability.toAvailability()
                val orderType = if (availability == ProductAvailability.IN_STOCK) OrderType.READY else OrderType.PREBOOK
                val status = if (availability == ProductAvailability.IN_STOCK) OrderStatus.CONFIRMED else OrderStatus.RESERVED

                transaction.set(
                    orders.document(orderId),
                    OrderDocument(
                        orderId = orderId,
                        userId = userId,
                        cooperativeId = FirestorePaths.DEFAULT_COOPERATIVE_ID,
                        status = status.name,
                        orderType = orderType.name,
                        totalItems = totalItems,
                        totalAmount = totalAmount,
                        createdAt = now,
                        updatedAt = now,
                        expectedDispatchDate = orderSnapshot.expectedDispatchDate,
                    ),
                )

                transaction.delete(cartRef)

                CheckoutResult(orderId = orderId, status = status, orderType = orderType)
            }
        }
    }

    override fun observeBuyerOrders(userId: String): Flow<List<Order>> = observeOrders(
        query = orders.whereEqualTo("userId", userId),
    )

    override fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val orderRef = orders.document(orderId)
        var latestOrder: OrderDocument? = null
        var latestItems: List<OrderItem> = emptyList()

        fun emitOrder() {
            val orderSnapshot = latestOrder ?: return
            trySend(
                Order(
                    orderId = orderSnapshot.orderId.ifBlank { orderId },
                    userId = orderSnapshot.userId,
                    cooperativeId = orderSnapshot.cooperativeId,
                    items = latestItems,
                    status = orderSnapshot.status.toOrderStatus(),
                    orderType = orderSnapshot.orderType.toOrderType(),
                    totalItems = orderSnapshot.totalItems.toInt(),
                    totalAmount = orderSnapshot.totalAmount.toFloat(),
                    createdAt = orderSnapshot.createdAt,
                    updatedAt = orderSnapshot.updatedAt,
                    expectedDispatchDate = orderSnapshot.expectedDispatchDate,
                ),
            )
        }

        val orderListener = orderRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    trySend(null)
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            latestOrder = snapshot?.toObject(OrderDocument::class.java)
            emitOrder()
        }
        val itemListener = orderRef.collection(FirestorePaths.ORDER_ITEMS).addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    close(error)
                }
                return@addSnapshotListener
            }
            latestItems = snapshot?.documents
                ?.mapNotNull { it as? QueryDocumentSnapshot }
                ?.map { it.toOrderItemDomain() }
                .orEmpty()
            emitOrder()
        }

        awaitClose {
            orderListener.remove()
            itemListener.remove()
        }
    }

    override fun observeLeaderOrders(cooperativeId: String): Flow<List<Order>> = observeOrders(
        query = orders.whereEqualTo("cooperativeId", cooperativeId),
    )

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        awaitTask {
            orders.document(orderId).update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun observeOrders(query: Query): Flow<List<Order>> = callbackFlow {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    trySend(emptyList())
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            launch {
                try {
                    val orders = snapshot?.documents?.mapNotNull { document ->
                        val items = awaitTask {
                            document.reference.collection(FirestorePaths.ORDER_ITEMS).get()
                        }.documents.mapNotNull { it as? QueryDocumentSnapshot }
                            .map { it.toOrderItemDomain() }
                        document.toOrderDomain(items)
                    }.orEmpty()
                        .sortedByDescending { it.createdAt }
                    trySend(orders)
                } catch (e: Exception) {
                    Log.e("FirestoreOrderRepo", "Error fetching order items: ${e.message}")
                    trySend(emptyList())
                }
            }
        }
        awaitClose { listener.remove() }
    }

    private fun validateCartQuantity(product: ProductDocument, quantity: Int) {
        val availability = product.availability.toAvailability()
        when (availability) {
            ProductAvailability.IN_STOCK -> {
                if (quantity > product.availableStock) {
                    throw IllegalStateException("Only ${product.availableStock} items in stock")
                }
            }

            ProductAvailability.PREBOOK_OPEN -> {
                val remaining = product.preorderLimit - product.reservedStock
                if (quantity > remaining) {
                    throw IllegalStateException("Only $remaining preorder slots left")
                }
            }

            ProductAvailability.COMING_SOON -> {
                if (!product.isPrebookEnabled) {
                    throw IllegalStateException("Pre-booking is not yet open for ${product.name}")
                }
            }

            ProductAvailability.SOLD_OUT -> throw IllegalStateException("${product.name} is sold out")
        }
    }

    private fun validateCheckoutQuantity(product: ProductDocument, quantity: Int) {
        validateCartQuantity(product, quantity)
    }

    override suspend fun checkoutSingleItem(userId: String, productId: String, quantity: Int): CheckoutResult {
        require(quantity > 0) { "Quantity must be at least 1" }
        val productRef = products.document(productId)
        return awaitTask {
            firestore.runTransaction { transaction ->
                val product = transaction.get(productRef)
                    .toObject(ProductDocument::class.java)
                    ?: throw IllegalStateException("Product not found")
                validateCheckoutQuantity(product, quantity)

                val now = System.currentTimeMillis()
                val orderId = orders.document().id
                val availability = product.availability.toAvailability()
                val orderType = if (availability == ProductAvailability.IN_STOCK) OrderType.READY else OrderType.PREBOOK
                val status = if (availability == ProductAvailability.IN_STOCK) OrderStatus.CONFIRMED else OrderStatus.RESERVED

                when (availability) {
                    ProductAvailability.IN_STOCK -> {
                        transaction.update(
                            productRef,
                            mapOf(
                                "availableStock" to (product.availableStock - quantity),
                                "soldStock" to (product.soldStock + quantity),
                                "lastModifiedAt" to now,
                            ),
                        )
                    }

                    ProductAvailability.PREBOOK_OPEN,
                    ProductAvailability.COMING_SOON,
                    -> {
                        transaction.update(
                            productRef,
                            mapOf(
                                "reservedStock" to (product.reservedStock + quantity),
                                "lastModifiedAt" to now,
                            ),
                        )
                    }

                    ProductAvailability.SOLD_OUT -> throw IllegalStateException("${product.name} is sold out")
                }

                val orderRef = orders.document(orderId)
                transaction.set(
                    orderRef,
                    OrderDocument(
                        orderId = orderId,
                        userId = userId,
                        cooperativeId = FirestorePaths.DEFAULT_COOPERATIVE_ID,
                        status = status.name,
                        orderType = orderType.name,
                        totalItems = quantity.toLong(),
                        totalAmount = quantity.toDouble() * product.pricePerUnit,
                        createdAt = now,
                        updatedAt = now,
                        expectedDispatchDate = product.expectedDispatchDate,
                    ),
                )
                transaction.set(
                    orderRef.collection(FirestorePaths.ORDER_ITEMS).document(productId),
                    OrderItemDocument(
                        itemId = productId,
                        productId = productId,
                        productName = product.name,
                        quantity = quantity.toLong(),
                        pricePerUnit = product.pricePerUnit.toDouble(),
                        unit = product.unit,
                        imageUrl = product.imageUrls.firstOrNull(),
                        familyName = product.familyName,
                        village = product.village,
                        availability = product.availability,
                        expectedDispatchDate = product.expectedDispatchDate,
                    ),
                )

                CheckoutResult(orderId = orderId, status = status, orderType = orderType)
            }
        }
    }

    private suspend fun <T> awaitTask(block: () -> com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            block()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}

private fun String.toAvailability(): ProductAvailability = try {
    ProductAvailability.valueOf(this)
} catch (_: Exception) {
    ProductAvailability.IN_STOCK
}

private fun String.toOrderStatus(): OrderStatus = try {
    OrderStatus.valueOf(this)
} catch (_: Exception) {
    OrderStatus.PENDING
}

private fun String.toOrderType(): OrderType = try {
    OrderType.valueOf(this)
} catch (_: Exception) {
    OrderType.READY
}

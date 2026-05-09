package com.budakattu.sante.data.repository

import com.budakattu.sante.data.remote.firebase.CartDocument
import com.budakattu.sante.data.remote.firebase.CartItemDocument
import com.budakattu.sante.data.remote.firebase.FirestorePaths
import com.budakattu.sante.data.remote.firebase.OrderDocument
import com.budakattu.sante.data.remote.firebase.OrderItemDocument
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreOrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : OrderRepository {
    private val carts = firestore.collection(FirestorePaths.CARTS)
    private val orders = firestore.collection(FirestorePaths.ORDERS)
    private val products = firestore.collection(FirestorePaths.COOPERATIVES)
        .document(FirestorePaths.DEFAULT_COOPERATIVE_ID)
        .collection(FirestorePaths.PRODUCTS)

    override fun observeCart(userId: String): Flow<Cart?> = callbackFlow {
        val cartRef = carts.document(userId)
        val itemRef = cartRef.collection(FirestorePaths.ORDER_ITEMS)
        var latestCart: CartDocument? = null
        var latestItems: List<com.budakattu.sante.domain.model.CartItem> = emptyList()

        fun emitCart() {
            val cart = latestCart
            if (cart == null && latestItems.isEmpty()) {
                trySend(null)
            } else {
                trySend(
                    Cart(
                        userId = cart?.userId.orEmpty().ifBlank { userId },
                        items = latestItems,
                        totalItems = cart?.totalItems?.toInt() ?: latestItems.sumOf { it.quantity },
                        updatedAt = cart?.updatedAt ?: 0L,
                    ),
                )
            }
        }

        val cartListener = cartRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            latestCart = snapshot?.toObject(CartDocument::class.java)
            emitCart()
        }
        val itemListener = itemRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            latestItems = snapshot?.documents
                ?.mapNotNull { it as? QueryDocumentSnapshot }
                ?.map { it.toCartItemDomain() }
                .orEmpty()
            emitCart()
        }

        awaitClose {
            cartListener.remove()
            itemListener.remove()
        }
    }

    override suspend fun addToCart(userId: String, productId: String, quantity: Int) {
        require(quantity > 0) { "Quantity must be at least 1" }
        val cartRef = carts.document(userId)
        val itemRef = cartRef.collection(FirestorePaths.ORDER_ITEMS).document(productId)
        val productRef = products.document(productId)

        awaitTask {
            firestore.runTransaction { transaction ->
                val product = transaction.get(productRef).toObject(com.budakattu.sante.data.remote.firebase.ProductDocument::class.java)
                    ?: throw IllegalStateException("Product not found")
                val existing = transaction.get(itemRef).toObject(CartItemDocument::class.java)
                val nextQuantity = (existing?.quantity?.toInt() ?: 0) + quantity
                validateCartQuantity(product, nextQuantity)

                val now = System.currentTimeMillis()
                transaction.set(
                    itemRef,
                    CartItemDocument(
                        itemId = productId,
                        productId = productId,
                        productName = product.name,
                        quantity = nextQuantity.toLong(),
                        pricePerUnit = product.pricePerUnit,
                        unit = product.unit,
                        imageUrl = product.imageUrls.firstOrNull(),
                        availability = product.availability,
                        expectedDispatchDate = product.expectedDispatchDate,
                        familyName = product.familyName,
                        village = product.village,
                    ),
                )
                val cartSnapshot = transaction.get(cartRef)
                val currentTotal = cartSnapshot.getLong("totalItems")?.toInt() ?: 0
                transaction.set(
                    cartRef,
                    CartDocument(
                        userId = userId,
                        totalItems = (currentTotal + quantity).toLong(),
                        updatedAt = now,
                    ),
                )
                null
            }
        }
    }

    override suspend fun updateCartItemQuantity(userId: String, itemId: String, quantity: Int) {
        val cartRef = carts.document(userId)
        val itemRef = cartRef.collection(FirestorePaths.ORDER_ITEMS).document(itemId)
        val productRef = products.document(itemId)

        awaitTask {
            firestore.runTransaction { transaction ->
                val existing = transaction.get(itemRef).toObject(CartItemDocument::class.java)
                    ?: throw IllegalStateException("Cart item not found")
                val currentQuantity = existing.quantity.toInt()
                val delta = quantity - currentQuantity
                val cartSnapshot = transaction.get(cartRef)
                val currentTotal = cartSnapshot.getLong("totalItems")?.toInt() ?: currentQuantity
                if (quantity <= 0) {
                    transaction.delete(itemRef)
                    val nextTotal = (currentTotal - currentQuantity).coerceAtLeast(0)
                    if (nextTotal == 0) {
                        transaction.delete(cartRef)
                    } else {
                        transaction.set(cartRef, CartDocument(userId, nextTotal.toLong(), System.currentTimeMillis()))
                    }
                } else {
                    val product = transaction.get(productRef).toObject(com.budakattu.sante.data.remote.firebase.ProductDocument::class.java)
                        ?: throw IllegalStateException("Product not found")
                    validateCartQuantity(product, quantity)
                    transaction.set(
                        itemRef,
                        existing.copy(quantity = quantity.toLong()),
                    )
                    transaction.set(
                        cartRef,
                        CartDocument(
                            userId = userId,
                            totalItems = (currentTotal + delta).toLong(),
                            updatedAt = System.currentTimeMillis(),
                        ),
                    )
                }
                null
            }
        }
    }

    override suspend fun removeCartItem(userId: String, itemId: String) {
        updateCartItemQuantity(userId, itemId, 0)
    }

    override suspend fun checkout(userId: String): CheckoutResult {
        val cartRef = carts.document(userId)
        val cartItemsRef = cartRef.collection(FirestorePaths.ORDER_ITEMS)
        val cartItems = awaitTask { cartItemsRef.get() }.documents.mapNotNull { doc ->
            doc.toObject(CartItemDocument::class.java)?.copy(itemId = doc.id)
        }
        if (cartItems.isEmpty()) {
            throw IllegalStateException("Cart is empty")
        }

        return awaitTask {
            firestore.runTransaction { transaction ->
                var totalAmount = 0.0
                var totalItems = 0
                var hasReady = false
                var hasPrebook = false
                var expectedDispatchDate: String? = null

                cartItems.forEach { item ->
                    val productRef = products.document(item.productId)
                    val productSnapshot = transaction.get(productRef)
                    val product = productSnapshot.toObject(com.budakattu.sante.data.remote.firebase.ProductDocument::class.java)
                        ?: throw IllegalStateException("${item.productName} is no longer available")
                    validateCheckoutQuantity(product, item.quantity.toInt())

                    totalItems += item.quantity.toInt()
                    totalAmount += item.quantity * item.pricePerUnit

                    when (product.availability.toAvailability()) {
                        ProductAvailability.IN_STOCK -> {
                            hasReady = true
                            transaction.update(
                                productRef,
                                mapOf(
                                    "availableStock" to (product.availableStock - item.quantity),
                                    "soldStock" to (product.soldStock + item.quantity),
                                    "lastModifiedAt" to System.currentTimeMillis(),
                                ),
                            )
                        }

                        ProductAvailability.PREBOOK_OPEN,
                        ProductAvailability.COMING_SOON,
                        -> {
                            hasPrebook = true
                            expectedDispatchDate = expectedDispatchDate ?: product.expectedDispatchDate
                            transaction.update(
                                productRef,
                                mapOf(
                                    "reservedStock" to (product.reservedStock + item.quantity),
                                    "lastModifiedAt" to System.currentTimeMillis(),
                                ),
                            )
                        }

                        ProductAvailability.SOLD_OUT -> throw IllegalStateException("${item.productName} is sold out")
                    }
                }

                val orderId = orders.document().id
                val orderType = when {
                    hasReady && hasPrebook -> OrderType.MIXED
                    hasPrebook -> OrderType.PREBOOK
                    else -> OrderType.READY
                }
                val status = if (hasPrebook) OrderStatus.RESERVED else OrderStatus.CONFIRMED
                val now = System.currentTimeMillis()
                val orderRef = orders.document(orderId)
                transaction.set(
                    orderRef,
                    OrderDocument(
                        orderId = orderId,
                        userId = userId,
                        cooperativeId = FirestorePaths.DEFAULT_COOPERATIVE_ID,
                        status = status.name,
                        orderType = orderType.name,
                        totalItems = totalItems.toLong(),
                        totalAmount = totalAmount,
                        createdAt = now,
                        updatedAt = now,
                        expectedDispatchDate = expectedDispatchDate,
                    ),
                )
                cartItems.forEach { item ->
                    transaction.set(
                        orderRef.collection(FirestorePaths.ORDER_ITEMS).document(item.itemId),
                        OrderItemDocument(
                            itemId = item.itemId,
                            productId = item.productId,
                            productName = item.productName,
                            quantity = item.quantity,
                            pricePerUnit = item.pricePerUnit,
                            unit = item.unit,
                            imageUrl = item.imageUrl,
                            familyName = item.familyName,
                            village = item.village,
                            availability = item.availability,
                            expectedDispatchDate = item.expectedDispatchDate,
                        ),
                    )
                    transaction.delete(cartItemsRef.document(item.itemId))
                }
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
                close(error)
                return@addSnapshotListener
            }
            latestOrder = snapshot?.toObject(OrderDocument::class.java)
            emitOrder()
        }
        val itemListener = orderRef.collection(FirestorePaths.ORDER_ITEMS).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
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
        val orderRef = orders.document(orderId)
        val items = awaitTask { orderRef.collection(FirestorePaths.ORDER_ITEMS).get() }
            .documents.mapNotNull { it.toObject(OrderItemDocument::class.java) }
        awaitTask {
            firestore.runTransaction { transaction ->
                val orderSnapshot = transaction.get(orderRef)
                val current = orderSnapshot.toObject(OrderDocument::class.java)
                    ?: throw IllegalStateException("Order not found")
                val currentStatus = current.status.toOrderStatus()
                if (currentStatus == status) {
                    return@runTransaction null
                }

                when (status) {
                    OrderStatus.CANCELLED -> {
                        items.forEach { item ->
                            val productRef = products.document(item.productId)
                            val product = transaction.get(productRef)
                                .toObject(com.budakattu.sante.data.remote.firebase.ProductDocument::class.java)
                                ?: return@forEach
                            when (item.availability.toAvailability()) {
                                ProductAvailability.IN_STOCK -> {
                                    transaction.update(
                                        productRef,
                                        mapOf(
                                            "availableStock" to (product.availableStock + item.quantity),
                                            "soldStock" to (product.soldStock - item.quantity).coerceAtLeast(0),
                                            "lastModifiedAt" to System.currentTimeMillis(),
                                        ),
                                    )
                                }

                                ProductAvailability.PREBOOK_OPEN,
                                ProductAvailability.COMING_SOON,
                                -> {
                                    transaction.update(
                                        productRef,
                                        mapOf(
                                            "reservedStock" to (product.reservedStock - item.quantity).coerceAtLeast(0),
                                            "lastModifiedAt" to System.currentTimeMillis(),
                                        ),
                                    )
                                }

                                ProductAvailability.SOLD_OUT -> Unit
                            }
                        }
                    }

                    OrderStatus.COMPLETED -> {
                        if (currentStatus == OrderStatus.RESERVED || currentStatus == OrderStatus.CONFIRMED) {
                            items.forEach { item ->
                                if (item.availability.toAvailability() != ProductAvailability.IN_STOCK) {
                                    val productRef = products.document(item.productId)
                                    val product = transaction.get(productRef)
                                        .toObject(com.budakattu.sante.data.remote.firebase.ProductDocument::class.java)
                                        ?: return@forEach
                                    transaction.update(
                                        productRef,
                                        mapOf(
                                            "reservedStock" to (product.reservedStock - item.quantity).coerceAtLeast(0),
                                            "soldStock" to (product.soldStock + item.quantity),
                                            "lastModifiedAt" to System.currentTimeMillis(),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    OrderStatus.PENDING,
                    OrderStatus.RESERVED,
                    OrderStatus.CONFIRMED,
                    -> Unit
                }

                transaction.update(
                    orderRef,
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to System.currentTimeMillis(),
                    ),
                )
                null
            }
        }
    }

    private fun observeOrders(query: com.google.firebase.firestore.Query): Flow<List<Order>> = callbackFlow {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            launch {
                val orders = snapshot?.documents?.mapNotNull { document ->
                    val items = awaitTask {
                        document.reference.collection(FirestorePaths.ORDER_ITEMS).get()
                    }.documents.mapNotNull { it as? QueryDocumentSnapshot }
                        .map { it.toOrderItemDomain() }
                    document.toOrderDomain(items)
                }.orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(orders)
            }
        }
        awaitClose { listener.remove() }
    }

    private fun validateCartQuantity(
        product: com.budakattu.sante.data.remote.firebase.ProductDocument,
        quantity: Int,
    ) {
        when (product.availability.toAvailability()) {
            ProductAvailability.IN_STOCK -> {
                if (quantity > product.availableStock) {
                    throw IllegalStateException("Only ${product.availableStock} ${product.unit} available")
                }
            }

            ProductAvailability.PREBOOK_OPEN,
            ProductAvailability.COMING_SOON,
            -> {
                if (!product.isPrebookEnabled) {
                    throw IllegalStateException("Pre-booking is not enabled for this product")
                }
                val remaining = (product.preorderLimit - product.reservedStock).toInt()
                if (quantity > remaining) {
                    throw IllegalStateException("Only $remaining units remain for pre-booking")
                }
            }

            ProductAvailability.SOLD_OUT -> throw IllegalStateException("This product is sold out")
        }
    }

    private fun validateCheckoutQuantity(
        product: com.budakattu.sante.data.remote.firebase.ProductDocument,
        quantity: Int,
    ) {
        validateCartQuantity(product, quantity)
    }

    override suspend fun checkoutSingleItem(userId: String, productId: String, quantity: Int): com.budakattu.sante.domain.model.CheckoutResult {
        require(quantity > 0) { "Quantity must be at least 1" }
        val productRef = products.document(productId)
        return awaitTask {
            firestore.runTransaction { transaction ->
                val product = transaction.get(productRef)
                    .toObject(com.budakattu.sante.data.remote.firebase.ProductDocument::class.java)
                    ?: throw IllegalStateException("Product not found")
                validateCheckoutQuantity(product, quantity)

                val now = System.currentTimeMillis()
                val orderId = orders.document().id
                val availability = product.availability.toAvailability()
                val orderType = if (availability == com.budakattu.sante.domain.model.ProductAvailability.IN_STOCK) {
                    com.budakattu.sante.domain.model.OrderType.READY
                } else {
                    com.budakattu.sante.domain.model.OrderType.PREBOOK
                }
                val status = if (availability == com.budakattu.sante.domain.model.ProductAvailability.IN_STOCK) {
                    com.budakattu.sante.domain.model.OrderStatus.CONFIRMED
                } else {
                    com.budakattu.sante.domain.model.OrderStatus.RESERVED
                }

                when (availability) {
                    com.budakattu.sante.domain.model.ProductAvailability.IN_STOCK -> {
                        transaction.update(
                            productRef,
                            mapOf(
                                "availableStock" to (product.availableStock - quantity),
                                "soldStock" to (product.soldStock + quantity),
                                "lastModifiedAt" to now,
                            ),
                        )
                    }

                    com.budakattu.sante.domain.model.ProductAvailability.PREBOOK_OPEN,
                    com.budakattu.sante.domain.model.ProductAvailability.COMING_SOON,
                    -> {
                        transaction.update(
                            productRef,
                            mapOf(
                                "reservedStock" to (product.reservedStock + quantity),
                                "lastModifiedAt" to now,
                            ),
                        )
                    }

                    com.budakattu.sante.domain.model.ProductAvailability.SOLD_OUT -> throw IllegalStateException("${product.name} is sold out")
                }

                val orderRef = orders.document(orderId)
                transaction.set(
                    orderRef,
                    com.budakattu.sante.data.remote.firebase.OrderDocument(
                        orderId = orderId,
                        userId = userId,
                        cooperativeId = FirestorePaths.DEFAULT_COOPERATIVE_ID,
                        status = status.name,
                        orderType = orderType.name,
                        totalItems = quantity.toLong(),
                        totalAmount = quantity * product.pricePerUnit,
                        createdAt = now,
                        updatedAt = now,
                        expectedDispatchDate = product.expectedDispatchDate,
                    ),
                )
                transaction.set(
                    orderRef.collection(FirestorePaths.ORDER_ITEMS).document(productId),
                    com.budakattu.sante.data.remote.firebase.OrderItemDocument(
                        itemId = productId,
                        productId = productId,
                        productName = product.name,
                        quantity = quantity.toLong(),
                        pricePerUnit = product.pricePerUnit,
                        unit = product.unit,
                        imageUrl = product.imageUrls.firstOrNull(),
                        familyName = product.familyName,
                        village = product.village,
                        availability = product.availability,
                        expectedDispatchDate = product.expectedDispatchDate,
                    ),
                )

                com.budakattu.sante.domain.model.CheckoutResult(orderId = orderId, status = status, orderType = orderType)
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

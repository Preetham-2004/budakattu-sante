package com.budakattu.sante.feature.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.Cart
import com.budakattu.sante.domain.model.CartItem
import com.budakattu.sante.domain.model.Order
import com.budakattu.sante.domain.model.OrderItem
import com.budakattu.sante.domain.model.OrderStatus
import com.budakattu.sante.domain.model.OrderType
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.usecase.order.CheckoutUseCase
import com.budakattu.sante.domain.usecase.order.ObserveBuyerOrdersUseCase
import com.budakattu.sante.domain.usecase.order.ObserveCartUseCase
import com.budakattu.sante.domain.usecase.order.ObserveLeaderOrdersUseCase
import com.budakattu.sante.domain.usecase.order.ObserveOrderUseCase
import com.budakattu.sante.domain.usecase.order.RemoveCartItemUseCase
import com.budakattu.sante.domain.usecase.order.UpdateCartItemQuantityUseCase
import com.budakattu.sante.domain.usecase.order.UpdateOrderStatusUseCase
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeCartUseCase: ObserveCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase,
    private val checkoutUseCase: CheckoutUseCase,
) : ViewModel() {
    private val sessionState = observeSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    val uiState = sessionState.flatMapLatest { session ->
        when (session) {
            is SessionState.LoggedIn -> observeCartUseCase(session.userId)
                .flatMapLatest { cart -> flowOf(cart.toUiState()) }
                .catch { emit(CartUiState.Error(it.localizedMessage ?: "Unable to load cart")) }
            SessionState.Loading -> flowOf(CartUiState.Loading)
            is SessionState.LoggedOut -> flowOf(CartUiState.Unauthenticated)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState.Loading)

    private val _events = MutableSharedFlow<CartEvent>()
    val events = _events.asSharedFlow()

    fun increaseQuantity(itemId: String, currentQuantity: Int) = updateQuantity(itemId, currentQuantity + 1)

    fun decreaseQuantity(itemId: String, currentQuantity: Int) {
        if (currentQuantity <= 1) {
            removeItem(itemId)
        } else {
            updateQuantity(itemId, currentQuantity - 1)
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            val session = sessionState.value as? SessionState.LoggedIn ?: return@launch
            runCatching {
                removeCartItemUseCase(session.userId, itemId)
            }.onFailure { error ->
                _events.emit(CartEvent.ShowMessage(error.localizedMessage ?: "Unable to remove item"))
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            val session = sessionState.value as? SessionState.LoggedIn ?: return@launch
            runCatching {
                checkoutUseCase(session.userId)
            }.onSuccess { result ->
                _events.emit(CartEvent.NavigateToConfirmation(result.orderId))
            }.onFailure { error ->
                _events.emit(CartEvent.ShowMessage(error.localizedMessage ?: "Checkout failed"))
            }
        }
    }

    private fun updateQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            val session = sessionState.value as? SessionState.LoggedIn ?: return@launch
            runCatching {
                updateCartItemQuantityUseCase(session.userId, itemId, quantity)
            }.onFailure { error ->
                _events.emit(CartEvent.ShowMessage(error.localizedMessage ?: "Unable to update quantity"))
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BuyerOrdersViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeBuyerOrdersUseCase: ObserveBuyerOrdersUseCase,
) : ViewModel() {
    val uiState = observeSessionUseCase().flatMapLatest { session ->
        when (session) {
            is SessionState.LoggedIn -> observeBuyerOrdersUseCase(session.userId)
                .flatMapLatest { orders ->
                    flowOf<BuyerOrdersUiState>(BuyerOrdersUiState.Content(orders.map(::toOrderCardUi)))
                }
                .catch { emit(BuyerOrdersUiState.Error(it.localizedMessage ?: "Unable to load orders")) }
            SessionState.Loading -> flowOf(BuyerOrdersUiState.Loading)
            is SessionState.LoggedOut -> flowOf(BuyerOrdersUiState.Unauthenticated)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BuyerOrdersUiState.Loading)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeOrderUseCase: ObserveOrderUseCase,
) : ViewModel() {
    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()

    val uiState = observeOrderUseCase(orderId)
        .flatMapLatest { order ->
            flowOf(
                when (order) {
                    null -> OrderDetailUiState.Error("Order not found")
                    else -> OrderDetailUiState.Content(order.toDetailUi())
                },
            )
        }
        .catch { emit(OrderDetailUiState.Error(it.localizedMessage ?: "Unable to load order")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderDetailUiState.Loading)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeaderOrdersViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeLeaderOrdersUseCase: ObserveLeaderOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
) : ViewModel() {
    private val sessionState = observeSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    val uiState = sessionState.flatMapLatest { session ->
        when (session) {
            is SessionState.LoggedIn -> {
                val cooperativeId = session.cooperativeId ?: return@flatMapLatest flowOf(LeaderOrdersUiState.Error("Missing cooperative access"))
                observeLeaderOrdersUseCase(cooperativeId)
                    .flatMapLatest { orders ->
                        flowOf<LeaderOrdersUiState>(
                            LeaderOrdersUiState.Content(
                                orders.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
                                    .map(::toLeaderOrderUi),
                            ),
                        )
                    }
                    .catch { emit(LeaderOrdersUiState.Error(it.localizedMessage ?: "Unable to load pending orders")) }
            }
            SessionState.Loading -> flowOf(LeaderOrdersUiState.Loading)
            is SessionState.LoggedOut -> flowOf(LeaderOrdersUiState.Error("Please sign in"))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaderOrdersUiState.Loading)

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            runCatching { updateOrderStatusUseCase(orderId, status) }
                .onFailure { error ->
                    _events.emit(error.localizedMessage ?: "Unable to update order status")
                }
        }
    }
}

sealed interface CartUiState {
    data object Loading : CartUiState
    data object Unauthenticated : CartUiState
    data object Empty : CartUiState
    data class Error(val message: String) : CartUiState
    data class Content(val cart: CartUi) : CartUiState
}

sealed interface BuyerOrdersUiState {
    data object Loading : BuyerOrdersUiState
    data object Unauthenticated : BuyerOrdersUiState
    data class Error(val message: String) : BuyerOrdersUiState
    data class Content(val orders: List<OrderCardUi>) : BuyerOrdersUiState
}

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Content(val order: OrderDetailUi) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

sealed interface LeaderOrdersUiState {
    data object Loading : LeaderOrdersUiState
    data class Content(val orders: List<LeaderOrderUi>) : LeaderOrdersUiState
    data class Error(val message: String) : LeaderOrdersUiState
}

sealed interface CartEvent {
    data class NavigateToConfirmation(val orderId: String) : CartEvent
    data class ShowMessage(val message: String) : CartEvent
}

data class CartUi(
    val items: List<CartItemUi>,
    val totalItems: Int,
    val totalAmountLabel: String,
)

data class CartItemUi(
    val itemId: String,
    val productName: String,
    val quantity: Int,
    val priceLabel: String,
    val dispatchLabel: String?,
    val availabilityLabel: String,
    val sourceLabel: String,
    val imageUrl: String?,
)

data class OrderCardUi(
    val orderId: String,
    val statusLabel: String,
    val typeLabel: String,
    val summary: String,
    val totalAmountLabel: String,
    val dispatchLabel: String?,
)

data class OrderDetailUi(
    val orderId: String,
    val statusLabel: String,
    val typeLabel: String,
    val totalAmountLabel: String,
    val dispatchLabel: String?,
    val items: List<OrderItemUi>,
)

data class OrderItemUi(
    val productName: String,
    val quantityLabel: String,
    val sourceLabel: String,
    val dispatchLabel: String?,
)

data class LeaderOrderUi(
    val orderId: String,
    val buyerLabel: String,
    val status: OrderStatus,
    val statusLabel: String,
    val typeLabel: String,
    val summary: String,
    val dispatchLabel: String?,
)

private fun Cart?.toUiState(): CartUiState {
    if (this == null || items.isEmpty()) return CartUiState.Empty
    val totalAmount = items.sumOf { it.quantity * it.pricePerUnit.toDouble() }
    val totalCount = items.sumOf { it.quantity }
    return CartUiState.Content(
        cart = CartUi(
            items = items.map(::toCartItemUi),
            totalItems = totalCount,
            totalAmountLabel = "Rs ${totalAmount.toInt()}",
        ),
    )
}

private fun toCartItemUi(item: CartItem): CartItemUi {
    return CartItemUi(
        itemId = item.itemId,
        productName = item.productName,
        quantity = item.quantity,
        priceLabel = "Rs ${item.pricePerUnit.toInt()}/${item.unit}",
        dispatchLabel = item.expectedDispatchDate,
        availabilityLabel = item.availability.name.replace("_", " "),
        sourceLabel = "${item.familyName}, ${item.village}",
        imageUrl = item.imageUrl,
    )
}

private fun toOrderCardUi(order: Order): OrderCardUi {
    return OrderCardUi(
        orderId = order.orderId,
        statusLabel = order.status.name.replace("_", " "),
        typeLabel = order.orderType.name.replace("_", " "),
        summary = "${order.totalItems} item(s) from ${order.items.firstOrNull()?.familyName ?: "cooperative"}",
        totalAmountLabel = "Rs ${order.totalAmount.toInt()}",
        dispatchLabel = order.expectedDispatchDate,
    )
}

private fun Order.toDetailUi(): OrderDetailUi {
    return OrderDetailUi(
        orderId = orderId,
        statusLabel = status.name.replace("_", " "),
        typeLabel = orderType.name.replace("_", " "),
        totalAmountLabel = "Rs ${totalAmount.toInt()}",
        dispatchLabel = expectedDispatchDate,
        items = items.map(::toOrderItemUi),
    )
}

private fun toOrderItemUi(item: OrderItem): OrderItemUi {
    return OrderItemUi(
        productName = item.productName,
        quantityLabel = "${item.quantity} ${item.unit}",
        sourceLabel = "${item.familyName}, ${item.village}",
        dispatchLabel = item.expectedDispatchDate,
    )
}

private fun toLeaderOrderUi(order: Order): LeaderOrderUi {
    return LeaderOrderUi(
        orderId = order.orderId,
        buyerLabel = order.userId,
        status = order.status,
        statusLabel = order.status.name.replace("_", " "),
        typeLabel = order.orderType.name.replace("_", " "),
        summary = "${order.totalItems} item(s) | ${order.items.joinToString { it.productName }}",
        dispatchLabel = order.expectedDispatchDate,
    )
}

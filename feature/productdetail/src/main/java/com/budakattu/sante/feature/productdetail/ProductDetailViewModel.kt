package com.budakattu.sante.feature.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.repository.PaymentGateway
import com.budakattu.sante.domain.repository.PaymentResult
import com.budakattu.sante.domain.usecase.order.AddToCartUseCase
import com.budakattu.sante.domain.usecase.order.CheckoutSingleItemUseCase
import com.budakattu.sante.domain.usecase.product.GetProductUseCase
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import com.budakattu.sante.domain.usecase.family.GetFamilyUseCase
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import com.budakattu.sante.domain.util.MspValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.util.UUID
import kotlinx.coroutines.channels.Channel

sealed class ProductDetailEvent {
    data class ShowSnackbar(val message: String) : ProductDetailEvent()
    data object OrderSuccess : ProductDetailEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getProductUseCase: GetProductUseCase,
    getProductsUseCase: GetProductsUseCase,
    getFamilyUseCase: GetFamilyUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val checkoutSingleItemUseCase: CheckoutSingleItemUseCase,
    private val paymentGateway: PaymentGateway,
    private val networkMonitor: NetworkMonitor,
    private val mspValidator: MspValidator,
) : ViewModel() {
    private val productId: String = savedStateHandle.get<String>("productId").orEmpty()
    private val sessionState = observeSessionUseCase()

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ProductDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentProductPrice: Float = 0f

    init {
        viewModelScope.launch {
            try {
                getProductUseCase(productId)
                    .flatMapLatest { product ->
                        if (product == null) {
                            flowOf(ProductDetailUiState.Error("Product not found"))
                        } else {
                            currentProductPrice = product.pricePerUnit
                            val familyFlow = if (product.familyId.isNotBlank()) {
                                getFamilyUseCase(product.familyId).catch { e ->
                                    Log.e("ProductDetail", "Error fetching family: ${e.message}")
                                    emit(null) 
                                }
                            } else {
                                flowOf(null)
                            }
                            
                            combine(
                                getProductsUseCase().catch { emit(emptyList()) },
                                familyFlow,
                            ) { allProducts, family ->
                                val currentSuccess = _uiState.value as? ProductDetailUiState.Success
                                ProductDetailUiState.Success(
                                    product = ProductDetailUi(
                                        id = product.productId,
                                        name = product.name,
                                        categoryName = product.categoryName,
                                        description = product.description,
                                        audioDescription = product.audioDescription,
                                        priceLabel = "Rs ${product.pricePerUnit.toInt()} per ${product.unit}",
                                        pricePerUnit = product.pricePerUnit,
                                        unit = product.unit,
                                        mspLabel = "MSP Rs ${product.mspPerUnit.toInt()} per ${product.unit}",
                                        isMspSafe = !mspValidator.isBelowMsp(product.pricePerUnit, product.mspPerUnit),
                                        stockLabel = when (product.availability) {
                                            ProductAvailability.IN_STOCK -> "${product.availableStock} ${product.unit} available now"
                                            ProductAvailability.PREBOOK_OPEN -> "${product.reservedStock}/${product.preorderLimit} units reserved"
                                            ProductAvailability.COMING_SOON -> "Manufacturing or harvest is not complete yet"
                                            ProductAvailability.SOLD_OUT -> "This batch is currently closed"
                                        },
                                        availabilityLabel = product.availability.name.replace("_", " "),
                                        ctaLabel = when (product.availability) {
                                            ProductAvailability.SOLD_OUT -> "Unavailable"
                                            else -> "Secure payment"
                                        },
                                        expectedDispatchLabel = product.expectedDispatchDate,
                                        seasonLabel = product.season,
                                        familyTitle = product.familyName,
                                        village = product.village,
                                        traceabilityLabel = "Batch tracked to ${product.familyName}, ${product.village}",
                                        harvestWindow = product.season ?: "Available year-round",
                                        familyDetails = family,
                                        imageUrls = product.imageUrls.ifEmpty {
                                            listOf("https://images.unsplash.com/photo-1471943311424-646960669fbc?w=900")
                                        },
                                    ),
                                    relatedProducts = allProducts
                                        .filterNot { it.productId == product.productId }
                                        .take(3)
                                        .map {
                                            RelatedProductUi(
                                                id = it.productId,
                                                title = it.name,
                                                subtitle = "${it.familyName} - ${it.categoryName}",
                                            )
                                        },
                                    isOffline = !networkMonitor.isOnline,
                                    isProcessingPayment = currentSuccess?.isProcessingPayment ?: false
                                )
                            }
                        }
                    }
                    .catch { error ->
                        Log.e("ProductDetail", "Flow error: ${error.message}", error)
                        emit(ProductDetailUiState.Error(error.localizedMessage ?: "Unknown error"))
                    }
                    .collect { _uiState.value = it }
            } catch (e: Exception) {
                Log.e("ProductDetail", "Launch exception: ${e.message}", e)
                _uiState.value = ProductDetailUiState.Error(e.localizedMessage ?: "Critical error")
            }
        }
    }

    fun onAudioDescriptionClick() {
        viewModelScope.launch {
            _events.send(ProductDetailEvent.ShowSnackbar("Playing accessibility description."))
        }
    }

    fun onPaymentClick(quantity: Int) {
        val currentState = _uiState.value as? ProductDetailUiState.Success ?: return
        if (currentState.isProcessingPayment) return

        viewModelScope.launch {
            val session = sessionState.first()
            if (session !is SessionState.LoggedIn) {
                _events.send(ProductDetailEvent.ShowSnackbar("Please sign in to complete your purchase."))
                return@launch
            }

            _uiState.update { 
                if (it is ProductDetailUiState.Success) it.copy(isProcessingPayment = true) else it 
            }

            try {
                val totalAmount = currentProductPrice * quantity
                val result = paymentGateway.initiatePayment(
                    amount = totalAmount.toDouble(),
                    orderId = "ORD_${UUID.randomUUID().toString().take(6).uppercase()}",
                    customerName = session.name,
                    customerEmail = "", 
                    customerPhone = ""
                )

                when (result) {
                    is PaymentResult.Success -> {
                        val checkoutResult = runCatching {
                            checkoutSingleItemUseCase(
                                userId = session.userId,
                                productId = productId,
                                quantity = quantity
                            )
                        }
                        
                        if (checkoutResult.isSuccess) {
                            _events.send(ProductDetailEvent.OrderSuccess)
                        } else {
                            val error = checkoutResult.exceptionOrNull()
                            Log.e("ProductDetail", "Checkout error: ${error?.message}", error)
                            _events.send(ProductDetailEvent.ShowSnackbar("Error saving order: ${error?.localizedMessage ?: "Unknown error"}"))
                        }
                    }
                    is PaymentResult.Failure -> {
                        _events.send(ProductDetailEvent.ShowSnackbar("Payment failed: ${result.message}"))
                    }
                    PaymentResult.Cancelled -> {
                        _events.send(ProductDetailEvent.ShowSnackbar("Payment cancelled."))
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductDetail", "Payment process exception: ${e.message}", e)
                _events.send(ProductDetailEvent.ShowSnackbar("Checkout failed: ${e.localizedMessage ?: "Connection error"}"))
            } finally {
                _uiState.update { 
                    if (it is ProductDetailUiState.Success) it.copy(isProcessingPayment = false) else it 
                }
            }
        }
    }

    fun addToCart(quantity: Int) {
        viewModelScope.launch {
            addToCartInternal(quantity, notifySuccess = true)
        }
    }

    private suspend fun addToCartInternal(quantity: Int, notifySuccess: Boolean): Boolean {
        val userId = when (val session = sessionState.first()) {
            is SessionState.LoggedIn -> session.userId
            else -> {
                _events.send(ProductDetailEvent.ShowSnackbar("Please sign in to continue."))
                return false
            }
        }
        return runCatching {
            addToCartUseCase(userId = userId, productId = productId, quantity = quantity)
        }.onSuccess {
            if (notifySuccess) {
                _events.send(ProductDetailEvent.ShowSnackbar("Added to cart."))
            }
        }.onFailure { error ->
            _events.send(ProductDetailEvent.ShowSnackbar(error.localizedMessage ?: "Unable to add to cart"))
        }.isSuccess
    }
}

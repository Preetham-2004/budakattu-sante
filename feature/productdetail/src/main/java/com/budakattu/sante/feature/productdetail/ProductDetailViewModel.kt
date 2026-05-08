package com.budakattu.sante.feature.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.usecase.product.GetProductUseCase
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import com.budakattu.sante.domain.util.MspValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getProductUseCase: GetProductUseCase,
    getProductsUseCase: GetProductsUseCase,
    private val networkMonitor: NetworkMonitor,
    private val mspValidator: MspValidator,
) : ViewModel() {
    private val productId: String = savedStateHandle.get<String>("productId").orEmpty()

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                getProductUseCase(productId),
                getProductsUseCase(),
            ) { product, allProducts ->
                if (product == null) {
                    ProductDetailUiState.Error("Product not found")
                } else {
                    ProductDetailUiState.Success(
                        product = ProductDetailUi(
                            id = product.productId,
                            name = product.name,
                            categoryName = product.categoryName,
                            description = product.description,
                            audioDescription = product.audioDescription,
                            priceLabel = "Rs ${product.pricePerUnit.toInt()} per ${product.unit}",
                            mspLabel = "MSP Rs ${product.mspPerUnit.toInt()} per ${product.unit}",
                            isMspSafe = !mspValidator.isBelowMsp(product.pricePerUnit, product.mspPerUnit),
                            stockLabel = when (product.availability) {
                                ProductAvailability.IN_STOCK -> "${product.stockQty} ${product.unit} available now"
                                ProductAvailability.PREBOOK_OPEN -> "${product.currentPrebookCount}/${product.maxPrebookQuantity} units reserved"
                                ProductAvailability.COMING_SOON -> "Manufacturing or harvest is not complete yet"
                                ProductAvailability.SOLD_OUT -> "This batch is currently closed"
                            },
                            availabilityLabel = product.availability.name.replace("_", " "),
                            ctaLabel = when (product.availability) {
                                ProductAvailability.IN_STOCK -> "Buy now"
                                ProductAvailability.PREBOOK_OPEN,
                                ProductAvailability.COMING_SOON,
                                ProductAvailability.SOLD_OUT,
                                -> if (product.isPrebookEnabled) "Pre-book now" else "Unavailable"
                            },
                            expectedDispatchLabel = product.expectedDispatchDate,
                            seasonLabel = product.season,
                            familyTitle = product.familyName,
                            village = product.village,
                            traceabilityLabel = "Batch tracked to ${product.familyName}, ${product.village}",
                            harvestWindow = product.season ?: "Available year-round",
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
                    )
                }
            }.collect { _uiState.value = it }
        }
    }

    fun onAudioDescriptionClick() {
        viewModelScope.launch {
            _events.emit("Playing accessibility description.")
        }
    }

    fun onPreorderClick() {
        viewModelScope.launch {
            val state = _uiState.value as? ProductDetailUiState.Success ?: return@launch
            val action = if (state.product.ctaLabel.contains("Buy", ignoreCase = true)) {
                "Purchase"
            } else {
                "Pre-book"
            }
            _events.emit("$action request captured for ${state.product.name}.")
        }
    }
}

package com.budakattu.sante.feature.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                            priceLabel = "Rs ${product.pricePerUnit.toInt()} per ${product.unit}",
                            mspLabel = "MSP Rs ${product.mspPerUnit.toInt()} per ${product.unit}",
                            isMspSafe = !mspValidator.isBelowMsp(product.pricePerUnit, product.mspPerUnit),
                            stockLabel = "${product.stockQty} ${product.unit} available",
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
            _events.emit("Audio descriptions will use Gemini + TTS in the next phase.")
        }
    }

    fun onPreorderClick() {
        viewModelScope.launch {
            _events.emit("Preorder flow is the next business slice after auth and detail.")
        }
    }
}

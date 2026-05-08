package com.budakattu.sante.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import com.budakattu.sante.domain.usecase.product.SeedCatalogUseCase
import com.budakattu.sante.domain.util.MspValidator
import com.budakattu.sante.feature.catalog.ui.ProductListEvent
import com.budakattu.sante.feature.catalog.ui.ProductListUiState
import com.budakattu.sante.feature.catalog.ui.ProductUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val seedCatalogUseCase: SeedCatalogUseCase,
    private val networkMonitor: NetworkMonitor,
    private val mspValidator: MspValidator,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProductListEvent>()
    val events: SharedFlow<ProductListEvent> = _events.asSharedFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            runCatching { seedCatalogUseCase() }
            getProductsUseCase()
                .onStart { _uiState.value = ProductListUiState.Loading }
                .catch { error ->
                    _uiState.value = ProductListUiState.Error(error.localizedMessage ?: "Unknown error")
                }
                .collect { products ->
                    _uiState.value = if (products.isEmpty() && !networkMonitor.isOnline) {
                        ProductListUiState.Offline
                    } else {
                        ProductListUiState.Success(
                            products = products.map(::toUiModel),
                            isOffline = !networkMonitor.isOnline,
                        )
                    }
                }
        }
    }

    fun onProductClick(productId: String) {
        viewModelScope.launch {
            _events.emit(ProductListEvent.NavigateToDetail(productId))
        }
    }

    private fun toUiModel(product: Product): ProductUiModel {
        return ProductUiModel(
            id = product.productId,
            name = product.name,
            description = product.description,
            audioDescription = product.audioDescription,
            imageUrl = product.imageUrls.firstOrNull()
                ?: "https://images.unsplash.com/photo-1471943311424-646960669fbc?w=900",
            familyName = product.familyName,
            village = product.village,
            categoryName = product.categoryName,
            priceLabel = "Rs ${product.pricePerUnit.toInt()}/${product.unit}",
            stockLabel = when (product.availability) {
                ProductAvailability.IN_STOCK -> "${product.availableStock} ${product.unit} ready"
                ProductAvailability.PREBOOK_OPEN -> "${product.reservedStock}/${product.preorderLimit} reserved"
                ProductAvailability.COMING_SOON -> "Upcoming seasonal batch"
                ProductAvailability.SOLD_OUT -> "Currently unavailable"
            },
            availabilityLabel = product.availability.name.replace("_", " "),
            ctaLabel = when (product.availability) {
                ProductAvailability.IN_STOCK -> "Buy now"
                ProductAvailability.PREBOOK_OPEN,
                ProductAvailability.COMING_SOON,
                ProductAvailability.SOLD_OUT,
                -> if (product.isPrebookEnabled) "Pre-book now" else "View details"
            },
            expectedDispatchLabel = product.expectedDispatchDate,
            seasonLabel = product.season,
            isMspSafe = !mspValidator.isBelowMsp(product.pricePerUnit, product.mspPerUnit),
        )
    }
}

package com.budakattu.sante.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.repository.NetworkMonitor
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import com.budakattu.sante.domain.usecase.product.SeedCatalogUseCase
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import com.budakattu.sante.domain.util.MspValidator
import com.budakattu.sante.feature.catalog.ui.ProductListEvent
import com.budakattu.sante.feature.catalog.ui.ProductListUiState
import com.budakattu.sante.feature.catalog.ui.ProductUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductListViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
    seedCatalogUseCase: SeedCatalogUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    networkMonitor: NetworkMonitor,
    private val mspValidator: MspValidator,
) : ViewModel() {
    val uiState: StateFlow<ProductListUiState> = combine(
        getProductsUseCase(),
        observeSessionUseCase()
    ) { products, session ->
        if (products.isEmpty() && !networkMonitor.isOnline) {
            ProductListUiState.Offline
        } else {
            val user = (session as? SessionState.LoggedIn)
            ProductListUiState.Success(
                products = products.map(::toUiModel),
                isOffline = !networkMonitor.isOnline,
                userName = user?.name,
                userProfilePictureUrl = user?.profilePictureUrl
            )
        }
    }.onStart {
        runCatching { seedCatalogUseCase() }
    }.catch { error ->
        emit(ProductListUiState.Error(error.localizedMessage ?: "Unknown error"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductListUiState.Loading)

    private val _events = MutableSharedFlow<ProductListEvent>()
    val events: SharedFlow<ProductListEvent> = _events.asSharedFlow()

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

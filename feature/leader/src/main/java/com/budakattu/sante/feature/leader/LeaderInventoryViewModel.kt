package com.budakattu.sante.feature.leader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderInventoryViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeaderInventoryUiState>(LeaderInventoryUiState.Loading)
    val uiState: StateFlow<LeaderInventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            getProductsUseCase(includeDrafts = false).collect { products ->
                _uiState.value = LeaderInventoryUiState.Success(products)
            }
        }
    }
}

sealed interface LeaderInventoryUiState {
    data object Loading : LeaderInventoryUiState
    data class Success(val products: List<Product>) : LeaderInventoryUiState
    data class Error(val message: String) : LeaderInventoryUiState
}

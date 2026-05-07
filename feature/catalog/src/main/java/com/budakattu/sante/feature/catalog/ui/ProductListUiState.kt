package com.budakattu.sante.feature.catalog.ui

import androidx.compose.runtime.Immutable

sealed interface ProductListUiState {
    data object Loading : ProductListUiState
    data class Success(
        val products: List<ProductUiModel>,
        val isOffline: Boolean,
    ) : ProductListUiState
    data class Error(val message: String) : ProductListUiState
    data object Offline : ProductListUiState
}

@Immutable
data class ProductUiModel(
    val id: String,
    val name: String,
    val description: String,
    val familyName: String,
    val village: String,
    val categoryName: String,
    val priceLabel: String,
    val stockLabel: String,
    val seasonLabel: String?,
    val isMspSafe: Boolean,
)

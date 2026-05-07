package com.budakattu.sante.feature.productdetail

import androidx.compose.runtime.Immutable

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Success(
        val product: ProductDetailUi,
        val relatedProducts: List<RelatedProductUi>,
        val isOffline: Boolean,
    ) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

@Immutable
data class ProductDetailUi(
    val id: String,
    val name: String,
    val categoryName: String,
    val description: String,
    val priceLabel: String,
    val mspLabel: String,
    val isMspSafe: Boolean,
    val stockLabel: String,
    val seasonLabel: String?,
    val familyTitle: String,
    val village: String,
    val traceabilityLabel: String,
    val harvestWindow: String,
    val imageUrls: List<String>,
)

@Immutable
data class RelatedProductUi(
    val id: String,
    val title: String,
    val subtitle: String,
)

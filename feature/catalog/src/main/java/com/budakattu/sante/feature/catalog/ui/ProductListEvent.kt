package com.budakattu.sante.feature.catalog.ui

sealed interface ProductListEvent {
    data class NavigateToDetail(val productId: String) : ProductListEvent
}

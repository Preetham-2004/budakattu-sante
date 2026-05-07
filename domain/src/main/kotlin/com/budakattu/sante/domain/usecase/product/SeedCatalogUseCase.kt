package com.budakattu.sante.domain.usecase.product

import com.budakattu.sante.domain.repository.ProductRepository
import javax.inject.Inject

class SeedCatalogUseCase @Inject constructor(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke() {
        productRepository.seedProductsIfEmpty()
    }
}

package com.budakattu.sante.domain.usecase.product

import com.budakattu.sante.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
) {
    operator fun invoke() = productRepository.getProducts()
}

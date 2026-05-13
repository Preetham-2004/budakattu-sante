package com.budakattu.sante.domain.usecase.product

import com.budakattu.sante.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(productId: String) {
        productRepository.deleteProduct(productId)
    }
}

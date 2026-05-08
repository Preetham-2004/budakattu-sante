package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(userId: String, productId: String, quantity: Int) {
        orderRepository.addToCart(userId, productId, quantity)
    }
}

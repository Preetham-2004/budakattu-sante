package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class UpdateCartItemQuantityUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(userId: String, itemId: String, quantity: Int) {
        orderRepository.updateCartItemQuantity(userId, itemId, quantity)
    }
}

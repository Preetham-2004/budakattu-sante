package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class RemoveCartItemUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(userId: String, itemId: String) {
        orderRepository.removeCartItem(userId, itemId)
    }
}

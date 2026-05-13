package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.model.CheckoutResult
import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class CheckoutSingleItemUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(
        userId: String,
        productId: String,
        quantity: Int,
    ): CheckoutResult = orderRepository.checkoutSingleItem(userId, productId, quantity)
}

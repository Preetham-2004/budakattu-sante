package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.model.OrderStatus
import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(orderId: String, status: OrderStatus) {
        orderRepository.updateOrderStatus(orderId, status)
    }
}

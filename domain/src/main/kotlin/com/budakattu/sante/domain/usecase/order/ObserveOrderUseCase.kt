package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class ObserveOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    operator fun invoke(orderId: String) = orderRepository.observeOrder(orderId)
}

package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class ObserveBuyerOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    operator fun invoke(userId: String) = orderRepository.observeBuyerOrders(userId)
}

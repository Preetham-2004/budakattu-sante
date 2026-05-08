package com.budakattu.sante.domain.usecase.order

import com.budakattu.sante.domain.repository.OrderRepository
import javax.inject.Inject

class ObserveLeaderOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    operator fun invoke(cooperativeId: String) = orderRepository.observeLeaderOrders(cooperativeId)
}

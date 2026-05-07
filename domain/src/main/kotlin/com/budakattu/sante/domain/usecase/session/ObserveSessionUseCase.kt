package com.budakattu.sante.domain.usecase.session

import com.budakattu.sante.domain.repository.SessionRepository
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke() = sessionRepository.observeSession()
}

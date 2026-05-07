package com.budakattu.sante.domain.usecase.session

import com.budakattu.sante.domain.repository.SessionRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() = sessionRepository.completeOnboarding()
}

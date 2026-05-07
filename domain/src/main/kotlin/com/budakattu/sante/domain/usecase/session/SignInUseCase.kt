package com.budakattu.sante.domain.usecase.session

import com.budakattu.sante.domain.model.UserRole
import com.budakattu.sante.domain.repository.SessionRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(name: String, role: UserRole) = sessionRepository.signIn(name, role)
}

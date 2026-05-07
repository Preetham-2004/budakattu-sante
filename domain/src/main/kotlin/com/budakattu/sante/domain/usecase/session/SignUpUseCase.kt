package com.budakattu.sante.domain.usecase.session

import com.budakattu.sante.domain.model.UserRole
import com.budakattu.sante.domain.repository.SessionRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        role: UserRole,
    ) = sessionRepository.signUp(name, email, password, role)
}

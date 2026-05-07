package com.budakattu.sante.domain.usecase.session

import com.budakattu.sante.domain.repository.SessionRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(idToken: String) = sessionRepository.signInWithGoogle(idToken)
}

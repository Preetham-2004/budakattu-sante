package com.budakattu.sante.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data class LoggedOut(
        val onboardingCompleted: Boolean,
    ) : SessionState
    data class LoggedIn(
        val userId: String,
        val name: String,
        val role: UserRole,
        val cooperativeId: String?,
        val onboardingCompleted: Boolean,
    ) : SessionState
}

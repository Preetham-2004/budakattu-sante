package com.budakattu.sante.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data class LoggedOut(
        val onboardingCompleted: Boolean,
    ) : SessionState
    data class LoggedIn(
        val userId: String,
        val name: String,
        val email: String,
        val phoneNumber: String = "",
        val alternatePhoneNumber: String = "",
        val address: String = "",
        val landmark: String = "",
        val city: String = "",
        val district: String = "",
        val state: String = "",
        val pincode: String = "",
        val preferredLanguage: String = "",
        val deliveryInstructions: String = "",
        val role: UserRole,
        val profilePictureUrl: String? = null,
        val cooperativeId: String?,
        val onboardingCompleted: Boolean,
    ) : SessionState
}

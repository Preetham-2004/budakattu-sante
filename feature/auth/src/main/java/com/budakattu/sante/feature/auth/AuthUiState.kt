package com.budakattu.sante.feature.auth

import com.budakattu.sante.domain.model.UserRole

data class AuthUiState(
    val name: String = "",
    val selectedRole: UserRole = UserRole.BUYER,
    val isLoading: Boolean = false,
)

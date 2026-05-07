package com.budakattu.sante.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import com.budakattu.sante.domain.usecase.session.CompleteOnboardingUseCase
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import com.budakattu.sante.domain.usecase.session.SignInUseCase
import com.budakattu.sante.domain.usecase.session.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = observeSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateRole(role: UserRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            completeOnboardingUseCase()
        }
    }

    fun signIn() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)
            val name = current.name.ifBlank { if (current.selectedRole == UserRole.LEADER) "Leader" else "Buyer" }
            signInUseCase(name = name, role = current.selectedRole)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }
}

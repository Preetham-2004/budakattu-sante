package com.budakattu.sante.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import com.budakattu.sante.domain.usecase.session.CompleteOnboardingUseCase
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import com.budakattu.sante.domain.usecase.session.SignInUseCase
import com.budakattu.sante.domain.usecase.session.SignInWithGoogleUseCase
import com.budakattu.sante.domain.usecase.session.SignOutUseCase
import com.budakattu.sante.domain.usecase.session.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
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
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = observeSessionUseCase()
        .catch { emit(SessionState.LoggedOut(onboardingCompleted = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorMessage = null)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun updateRole(role: UserRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role, errorMessage = null)
    }

    fun updateProfilePictureUrl(url: String) {
        _uiState.value = _uiState.value.copy(profilePictureUrl = url, errorMessage = null)
    }

    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            completeOnboardingUseCase()
        }
    }

    fun signIn() {
        val current = _uiState.value
        val validationError = validateForSignIn(current)
        if (validationError != null) {
            _uiState.value = current.copy(errorMessage = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            runCatching {
                signInUseCase(email = current.email.trim(), password = current.password)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Unable to sign in",
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signUp() {
        val current = _uiState.value
        val validationError = validateForSignUp(current)
        if (validationError != null) {
            _uiState.value = current.copy(errorMessage = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            runCatching {
                signUpUseCase(
                    name = current.name.trim(),
                    email = current.email.trim(),
                    password = current.password,
                    profilePictureUri = current.profilePictureUrl.trim().takeIf { it.isNotBlank() },
                    role = current.selectedRole,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Unable to create account",
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                signInWithGoogleUseCase(idToken)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Unable to continue with Google",
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    private fun validateForSignIn(state: AuthUiState): String? {
        if (state.email.isBlank()) return "Email is required"
        if (!state.email.contains("@")) return "Enter a valid email address"
        if (state.password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    private fun validateForSignUp(state: AuthUiState): String? {
        if (state.name.isBlank()) return "Name is required"
        return validateForSignIn(state)
    }
}

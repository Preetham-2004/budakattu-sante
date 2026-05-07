package com.budakattu.sante.feature.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.theme.Parchment
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import kotlinx.coroutines.launch

@Composable
fun SplashRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToBuyerHome: () -> Unit,
    onNavigateToLeaderHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        when (val state = sessionState) {
            SessionState.Loading -> Unit
            is SessionState.LoggedIn -> {
                if (state.role == UserRole.LEADER) onNavigateToLeaderHome() else onNavigateToBuyerHome()
            }
            is SessionState.LoggedOut -> {
                if (state.onboardingCompleted) onNavigateToLogin() else onNavigateToOnboarding()
            }
        }
    }

    Surface(color = Parchment, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Budakattu-Sante", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    ContentFrame {
        ForestCard {
            Text("Forest-to-home marketplace", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Browse traceable forest products, support fair trade, and keep cooperative workflows reliable offline.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.completeOnboarding()
                    onContinue()
                },
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onSignup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedIn) {
            onLogin()
        }
    }

    AuthForm(
        title = "Login",
        buttonLabel = "Enter Marketplace",
        secondaryLabel = "Create account",
        onPrimaryAction = viewModel::signIn,
        onSecondaryAction = onSignup,
        viewModel = viewModel,
        showNameField = false,
        showRoleSelector = false,
    )
}

@Composable
fun SignupScreen(
    onSignupComplete: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedIn) {
            onSignupComplete()
        }
    }

    AuthForm(
        title = "Create account",
        buttonLabel = "Create account",
        secondaryLabel = "Already have an account",
        onPrimaryAction = viewModel::signUp,
        onSecondaryAction = onLogin,
        viewModel = viewModel,
        showNameField = true,
        showRoleSelector = true,
        showGoogleButton = false,
    )
}

@Composable
private fun AuthForm(
    title: String,
    buttonLabel: String,
    secondaryLabel: String,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    viewModel: AuthViewModel,
    showNameField: Boolean,
    showRoleSelector: Boolean,
    showGoogleButton: Boolean = true,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ContentFrame {
        ForestCard {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            if (showNameField) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
            )
            if (showRoleSelector) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Role", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                RoleSelector(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = viewModel::updateRole,
                )
            }
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = onPrimaryAction,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(buttonLabel)
                }
            }
            if (showGoogleButton) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    onClick = {
                        val activity = context.findActivity() ?: run {
                            viewModel.setErrorMessage("Google sign-in requires an active Android activity")
                            return@OutlinedButton
                        }
                        scope.launch {
                            runCatching {
                                requestGoogleIdToken(activity)
                            }.onSuccess { token ->
                                viewModel.signInWithGoogle(token)
                            }.onFailure { error ->
                                viewModel.setErrorMessage(
                                    error.localizedMessage ?: "Unable to continue with Google",
                                )
                            }
                        }
                    },
                ) {
                    Text("Continue with Google")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = onSecondaryAction,
            ) {
                Text(secondaryLabel)
            }
        }
    }
}

@Composable
private fun RoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onRoleSelected(UserRole.BUYER) },
        ) {
            Text(if (selectedRole == UserRole.BUYER) "Buyer selected" else "Continue as Buyer")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onRoleSelected(UserRole.LEADER) },
        ) {
            Text(if (selectedRole == UserRole.LEADER) "Leader selected" else "Continue as Leader")
        }
    }
}

@Composable
private fun ContentFrame(content: @Composable () -> Unit) {
    Scaffold(containerColor = Parchment) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
        ) {
            content()
        }
    }
}

private suspend fun requestGoogleIdToken(activity: Activity): String {
    val credentialManager = CredentialManager.create(activity)
    val serverClientId = activity.resolveGoogleWebClientId()

    val authorizedRequest = buildGoogleCredentialRequest(
        serverClientId = serverClientId,
        filterByAuthorizedAccounts = true,
    )

    val credential = runCatching {
        credentialManager.getCredential(activity, authorizedRequest).credential
    }.getOrElse {
        credentialManager.getCredential(
            activity,
            buildGoogleCredentialRequest(
                serverClientId = serverClientId,
                filterByAuthorizedAccounts = false,
            ),
        ).credential
    }

    if (
        credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        return try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (error: GoogleIdTokenParsingException) {
            throw IllegalStateException("Unable to parse Google credential", error)
        }
    }

    throw IllegalStateException("Google sign-in was cancelled or returned an unsupported credential")
}

private fun buildGoogleCredentialRequest(
    serverClientId: String,
    filterByAuthorizedAccounts: Boolean,
): GetCredentialRequest {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(serverClientId)
        .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
        .build()

    return GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun Context.resolveGoogleWebClientId(): String {
    val resourceId = resources.getIdentifier("default_web_client_id", "string", packageName)
    check(resourceId != 0) {
        "default_web_client_id was not found. Download the updated google-services.json after enabling Google sign-in."
    }
    return getString(resourceId)
}

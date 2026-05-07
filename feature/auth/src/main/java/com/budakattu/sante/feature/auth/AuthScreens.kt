package com.budakattu.sante.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.theme.Parchment
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole

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
    AuthForm(
        title = "Login",
        buttonLabel = "Enter Marketplace",
        secondaryLabel = "Create account",
        onPrimaryAction = {
            viewModel.signIn()
            onLogin()
        },
        onSecondaryAction = onSignup,
        viewModel = viewModel,
    )
}

@Composable
fun SignupScreen(
    onSignupComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    AuthForm(
        title = "Signup",
        buttonLabel = "Create local session",
        secondaryLabel = "Use selected role",
        onPrimaryAction = {
            viewModel.signIn()
            onSignupComplete()
        },
        onSecondaryAction = {},
        viewModel = viewModel,
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ContentFrame {
        ForestCard {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Role", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            RoleSelector(
                selectedRole = uiState.selectedRole,
                onRoleSelected = viewModel::updateRole,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = onPrimaryAction,
            ) {
                Text(buttonLabel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
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

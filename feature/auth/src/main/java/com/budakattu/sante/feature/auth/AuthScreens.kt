package com.budakattu.sante.feature.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.AmberHarvest
import com.budakattu.sante.core.ui.theme.BarkBrown
import com.budakattu.sante.core.ui.theme.CharcoalInk
import com.budakattu.sante.core.ui.theme.ClayBorder
import com.budakattu.sante.core.ui.theme.ForestPrimary
import com.budakattu.sante.core.ui.theme.LeafAccent
import com.budakattu.sante.core.ui.theme.MilletGold
import com.budakattu.sante.core.ui.theme.Parchment
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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

    HeritageScaffold(
        title = "Forest Market Awakens",
        subtitle = "Preparing your cooperative-grade marketplace with traceability, fair pricing, and rooted trade identity.",
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ForestCard {
                Text("Budakattu-Sante", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Traditional value. Modern market access.")
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
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
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                RouteBadge(label = "Access", value = "Buyer + Leader")
                RouteBadge(label = "Mode", value = "Offline-ready")
            }
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedIn) {
            onLogin()
        }
    }

    LoginExperience(
        uiState = uiState,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onPrimaryAction = viewModel::signIn,
        onGoogleAction = viewModel::signInWithGoogle,
        onSecondaryAction = onSignup,
        onError = viewModel::setErrorMessage,
    )
}

@Composable
fun SignupScreen(
    onSignupComplete: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.LoggedIn) {
            onSignupComplete()
        }
    }

    SignupExperience(
        uiState = uiState,
        onNameChange = viewModel::updateName,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onRoleChange = viewModel::updateRole,
        onPrimaryAction = viewModel::signUp,
        onSecondaryAction = onLogin,
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
            Text(
                text = if (showNameField) {
                    "Create your route into the forest market."
                } else {
                    "Secure your entry into a premium tribal marketplace."
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                            runCatching { requestGoogleIdToken(activity) }
                                .onSuccess(viewModel::signInWithGoogle)
                                .onFailure { error ->
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
private fun LoginExperience(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPrimaryAction: () -> Unit,
    onGoogleAction: (String) -> Unit,
    onSecondaryAction: () -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBF5), Color(0xFFF7EDDB), Color(0xFFFFFBF5)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            WelcomePanel()
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFFFFCF7),
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF0F5E5),
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = ForestPrimary,
                                )
                            }
                        }
                        Column {
                            Text("Login", style = MaterialTheme.typography.headlineMedium, color = ForestPrimary)
                            Text(
                                "Secure your entry into a premium tribal marketplace.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CharcoalInk.copy(alpha = 0.82f),
                            )
                        }
                    }

                    LoginFieldLabel("Email or Phone")
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email or phone number") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = BarkBrown,
                            )
                        },
                    )

                    LoginFieldLabel("Password")
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your password") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = BarkBrown,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = BarkBrown.copy(alpha = 0.7f),
                            )
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Forgot password?", color = ForestPrimary, style = MaterialTheme.typography.labelLarge)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("Need help?", color = ForestPrimary, style = MaterialTheme.typography.labelLarge)
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ForestPrimary,
                            )
                        }
                    }

                    uiState.errorMessage?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        onClick = onPrimaryAction,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Enter Marketplace")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text("OR", style = MaterialTheme.typography.labelLarge, color = BarkBrown)
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ClayBorder),
                        enabled = !uiState.isLoading,
                        onClick = {
                            val activity = context.findActivity() ?: run {
                                onError("Google sign-in requires an active Android activity")
                                return@OutlinedButton
                            }
                            scope.launch {
                                runCatching { requestGoogleIdToken(activity) }
                                    .onSuccess(onGoogleAction)
                                    .onFailure { error ->
                                        onError(error.localizedMessage ?: "Unable to continue with Google")
                                    }
                            }
                        },
                    ) {
                        Text("G", color = Color(0xFFDB4437), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue with Google", color = CharcoalInk)
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ClayBorder),
                        onClick = onSecondaryAction,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = CharcoalInk,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create new account", color = CharcoalInk)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ForestPrimary,
                        )
                        Text(
                            "Your data is safe with us. We value your trust.",
                            style = MaterialTheme.typography.labelLarge,
                            color = ForestPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignupExperience(
    uiState: AuthUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBF5), Color(0xFFF7EDDB), Color(0xFFFFFBF5)),
                ),
            ),
    ) {
        LoginBackgroundAccent()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            WelcomePanel(
                title = "Create your account",
                subtitle = "Join the forest marketplace and begin your trusted route into tribal products.",
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFFFFCF7),
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF0F5E5),
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    tint = ForestPrimary,
                                )
                            }
                        }
                        Column {
                            Text("Create account", style = MaterialTheme.typography.headlineMedium, color = ForestPrimary)
                            Text(
                                "Set up your marketplace access in a few quick steps.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CharcoalInk.copy(alpha = 0.82f),
                            )
                        }
                    }

                    LoginFieldLabel("Full Name")
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your full name") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                tint = BarkBrown,
                            )
                        },
                    )

                    LoginFieldLabel("Email or Phone")
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email or phone number") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = BarkBrown,
                            )
                        },
                    )

                    LoginFieldLabel("Password")
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Create a password") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = BarkBrown,
                            )
                        },
                    )

                    LoginFieldLabel("Choose Role")
                    RoleSelector(
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = onRoleChange,
                    )

                    uiState.errorMessage?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        onClick = onPrimaryAction,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Create account")
                        }
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ClayBorder),
                        onClick = onSecondaryAction,
                    ) {
                        Text("Already have an account", color = CharcoalInk)
                    }
                }
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
private fun BrandHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE8E2C6),
                border = BorderStroke(1.dp, ClayBorder),
                modifier = Modifier.size(46.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = ForestPrimary,
                    )
                }
            }
            Column {
                Text("BUDAKATTU SANTE", style = MaterialTheme.typography.titleLarge, color = ForestPrimary)
                Text(
                    "Forest-to-Home Tribal Marketplace",
                    style = MaterialTheme.typography.labelLarge,
                    color = CharcoalInk.copy(alpha = 0.75f),
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF2D8),
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = AmberHarvest,
                )
            }
        }
    }
}

@Composable
private fun WelcomePanel(
    title: String = "Welcome back!",
    subtitle: String = "Sign in to explore authentic forest products and support tribal communities.",
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = ForestPrimary,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = Parchment)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Parchment.copy(alpha = 0.92f),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AmberHarvest.copy(alpha = 0.14f),
                    modifier = Modifier.size(78.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MilletGold,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoBadge(
                    icon = Icons.Outlined.Storefront,
                    label = "MARKET",
                    value = "Live",
                )
                InfoBadge(
                    icon = Icons.Outlined.Lock,
                    label = "ACCESS",
                    value = "Secure",
                )
            }
        }
    }
}

@Composable
private fun InfoBadge(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = LeafAccent.copy(alpha = 0.22f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Parchment.copy(alpha = 0.92f),
            )
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, color = Parchment.copy(alpha = 0.8f))
                Text(value, style = MaterialTheme.typography.titleLarge, color = Parchment)
            }
        }
    }
}

@Composable
private fun LoginFieldLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = BarkBrown)
}

@Composable
private fun LoginBackgroundAccent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(120.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(MilletGold.copy(alpha = 0.12f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(148.dp)
                .clip(RoundedCornerShape(topStart = 90.dp))
                .background(AmberHarvest.copy(alpha = 0.08f)),
        )
    }
}

@Composable
private fun ContentFrame(
    content: @Composable () -> Unit,
) {
    HeritageScaffold(
        title = "Enter The Trading Circle",
        subtitle = "Use a secure path into the marketplace shaped for forest cooperatives and urban buyers.",
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 6.dp),
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

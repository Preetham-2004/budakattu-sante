package com.budakattu.sante.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.BuyerRouteStrip
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.ForestBackground
import com.budakattu.sante.core.ui.theme.ForestPrimary
import com.budakattu.sante.core.ui.theme.LeafAccent
import com.budakattu.sante.core.ui.theme.SunsetClay
import com.budakattu.sante.domain.model.OrderStatus
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.usecase.order.ObserveBuyerOrdersUseCase
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import com.budakattu.sante.domain.usecase.session.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun BuyerProfileRoute(
    activeRoute: String,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    viewModel: BuyerProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    HeritageScaffold(
        title = "Buyer Profile",
        subtitle = "Finish your details now and keep checkout faster later.",
        showBack = true,
        onBack = onBack,
        topBarContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onBack() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                }
                
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .padding(innerPadding),
            ) {
                when (val state = uiState) {
                    BuyerProfileUiState.Loading -> LoadingBody()
                    is BuyerProfileUiState.Error -> ErrorBody(state.message)
                    is BuyerProfileUiState.Content -> ProfileContent(
                        state = state,
                        activeRoute = activeRoute,
                        marketRoute = marketRoute,
                        cartRoute = cartRoute,
                        profileRoute = profileRoute,
                        onNavigate = onNavigate,
                        onSignOut = onSignOut,
                        onUpdateProfile = viewModel::updateProfile,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: BuyerProfileUiState.Content,
    activeRoute: String,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onUpdateProfile: (BuyerProfileFormState) -> Unit,
) {
    var isEditing by remember(state.profileCompletion) { mutableStateOf(state.profileCompletion < 1f) }
    var form by remember(state) { mutableStateOf(state.toFormState()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            BuyerRouteStrip(
                activeRoute = activeRoute,
                onNavigate = onNavigate,
                marketRoute = marketRoute,
                cartRoute = cartRoute,
                profileRoute = profileRoute,
            )
        }

        item {
            ProfileHeroCard(
                state = state,
                isEditing = isEditing,
            ) {
                form = state.toFormState()
                isEditing = !isEditing
            }
        }

        if (isEditing) {
            item {
        ProfileFormCard(
            form = form,
            isUpdating = state.isUpdating,
            onFormChange = { form = it },
        ) {
            onUpdateProfile(form)
            isEditing = false
        }
            }
        } else {
            item {
                CompletionPromptCard(
                    completion = state.profileCompletion,
                    missingFields = state.missingFields,
                    onEdit = { isEditing = true },
                )
            }
            item {
                ProfileSectionCard(
                    title = "Personal Details",
                    icon = Icons.Default.Person,
                ) {
                    DetailRow("Full name", state.name)
                    DetailRow("Email", state.email)
                    DetailRow("Phone", state.phoneNumber.ifBlank { "Not provided" })
                    DetailRow("Alternate phone", state.alternatePhoneNumber.ifBlank { "Not provided" })
                }
            }
            item {
                ProfileSectionCard(
                    title = "Delivery Details",
                    icon = Icons.Default.LocalShipping,
                ) {
                    DetailRow("Address", state.address.ifBlank { "Not provided" })
                    DetailRow("Landmark", state.landmark.ifBlank { "Not provided" })
                    DetailRow("City", state.city.ifBlank { "Not provided" })
                    DetailRow("District", state.district.ifBlank { "Not provided" })
                    DetailRow("State", state.state.ifBlank { "Not provided" })
                    DetailRow("Pincode", state.pincode.ifBlank { "Not provided" })
                    DetailRow("Delivery notes", state.deliveryInstructions.ifBlank { "Not provided" })
                }
            }
            item {
                ProfileSectionCard(
                    title = "Preferences",
                    icon = Icons.Default.Settings,
                ) {
                    DetailRow("Preferred language", state.preferredLanguage.ifBlank { "Not provided" })
                    DetailRow("Buyer ID", state.userId)
                }
            }
        }

        item {
            ForestCard {
                Text(
                    "Market Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RouteBadge(label = "Total Orders", value = state.totalOrders.toString())
                    RouteBadge(label = "Pending", value = state.activePreorders.toString())
                }
            }
        }

        item {
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Sign out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    state: BuyerProfileUiState.Content,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
) {
    ForestCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = state.profilePictureUrl ?: "https://i.pravatar.cc/150?u=${state.userId}",
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(ForestBackground),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.name.ifBlank { "Buyer account" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedAssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text("${(state.profileCompletion * 100).toInt()}% profile complete")
                    },
                )
            }
            IconButton(onClick = onToggleEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isEditing) "Close edit mode" else "Edit profile",
                    tint = ForestPrimary,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { state.profileCompletion },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = ForestPrimary,
            trackColor = LeafAccent.copy(alpha = 0.25f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (state.missingFields.isEmpty()) {
                "All important buyer details are available."
            } else {
                "Add ${state.missingFields.asSequence().take(3).joinToString()} to complete your buyer profile."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun CompletionPromptCard(
    completion: Float,
    missingFields: List<String>,
    onEdit: () -> Unit,
) {
    ForestCard {
        Text(
            text = if (completion >= 1f) "Profile ready" else "Complete your buyer details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (missingFields.isEmpty()) {
                "You can still update delivery or language preferences any time."
            } else {
                "Missing: ${missingFields.joinToString()}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        if (missingFields.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                missingFields.forEach { field ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = LeafAccent.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = field,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (completion >= 1f) "Update details" else "Complete profile")
        }
    }
}

@Composable
private fun ProfileFormCard(
    form: BuyerProfileFormState,
    isUpdating: Boolean,
    onFormChange: (BuyerProfileFormState) -> Unit,
    onSave: () -> Unit,
) {
    ForestCard {
        Text(
            "Buyer Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Users can sign up with only basic details. Finish the rest here for smoother delivery and support.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(18.dp))

        FormSectionTitle("Personal")
        ProfileField(
            value = form.name,
            label = "Full Name",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(name = it)) },
        )
        ProfileField(
            value = form.phoneNumber,
            label = "Primary Phone",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(phoneNumber = it)) },
        )
        ProfileField(
            value = form.alternatePhoneNumber,
            label = "Alternate Phone",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(alternatePhoneNumber = it)) },
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(10.dp))

        FormSectionTitle("Delivery")
        ProfileField(
            value = form.address,
            label = "Address",
            enabled = !isUpdating,
            singleLine = false,
            minLines = 2,
            onValueChange = { onFormChange(form.copy(address = it)) },
        )
        ProfileField(
            value = form.landmark,
            label = "Landmark",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(landmark = it)) },
        )
        ProfileField(
            value = form.city,
            label = "City / Town",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(city = it)) },
        )
        ProfileField(
            value = form.district,
            label = "District",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(district = it)) },
        )
        ProfileField(
            value = form.state,
            label = "State",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(state = it)) },
        )
        ProfileField(
            value = form.pincode,
            label = "Pincode",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(pincode = it)) },
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(10.dp))

        FormSectionTitle("Preferences")
        ProfileField(
            value = form.preferredLanguage,
            label = "Preferred Language",
            enabled = !isUpdating,
            onValueChange = { onFormChange(form.copy(preferredLanguage = it)) },
        )
        ProfileField(
            value = form.deliveryInstructions,
            label = "Delivery Instructions",
            enabled = !isUpdating,
            singleLine = false,
            minLines = 2,
            onValueChange = { onFormChange(form.copy(deliveryInstructions = it)) },
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !isUpdating,
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save buyer details", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    ForestCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LeafAccent.copy(alpha = 0.16f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = ForestPrimary,
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun FormSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ForestPrimary,
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun ProfileField(
    value: String,
    label: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LoadingBody() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ForestPrimary)
    }
}

@Composable
private fun ErrorBody(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        ForestCard {
            Text("Error", style = MaterialTheme.typography.titleLarge, color = SunsetClay)
            Text(message, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

data class BuyerProfileFormState(
    val name: String,
    val phoneNumber: String,
    val alternatePhoneNumber: String,
    val address: String,
    val landmark: String,
    val city: String,
    val district: String,
    val state: String,
    val pincode: String,
    val preferredLanguage: String,
    val deliveryInstructions: String,
)

private fun BuyerProfileUiState.Content.toFormState() = BuyerProfileFormState(
    name = name,
    phoneNumber = phoneNumber,
    alternatePhoneNumber = alternatePhoneNumber,
    address = address,
    landmark = landmark,
    city = city,
    district = district,
    state = state,
    pincode = pincode,
    preferredLanguage = preferredLanguage,
    deliveryInstructions = deliveryInstructions,
)

private fun calculateProfileCompletion(content: BuyerProfileUiState.Content): Pair<Float, List<String>> {
    val trackedFields = listOf(
        "phone" to content.phoneNumber,
        "address" to content.address,
        "city" to content.city,
        "district" to content.district,
        "state" to content.state,
        "pincode" to content.pincode,
        "language" to content.preferredLanguage,
    )
    val completed = trackedFields.count { (_, value) -> value.isNotBlank() }
    val missing = trackedFields.filter { (_, value) -> value.isBlank() }.map { (label, _) -> label }
    return (completed.toFloat() / trackedFields.size.toFloat()) to missing
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BuyerProfileViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    observeBuyerOrdersUseCase: ObserveBuyerOrdersUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : androidx.lifecycle.ViewModel() {

    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _isUpdating = kotlinx.coroutines.flow.MutableStateFlow(value = false)

    val uiState = observeSessionUseCase()
        .flatMapLatest { session ->
            when (session) {
                is SessionState.LoggedIn -> observeBuyerOrdersUseCase(session.userId)
                    .combine(flowOf(session)) { orders, loggedIn -> loggedIn to orders }
                    .combine(_isUpdating) { (loggedIn, orders), updating ->
                        val baseContent = BuyerProfileUiState.Content(
                            userId = loggedIn.userId,
                            name = loggedIn.name,
                            email = loggedIn.email,
                            phoneNumber = loggedIn.phoneNumber,
                            alternatePhoneNumber = loggedIn.alternatePhoneNumber,
                            address = loggedIn.address,
                            landmark = loggedIn.landmark,
                            city = loggedIn.city,
                            district = loggedIn.district,
                            state = loggedIn.state,
                            pincode = loggedIn.pincode,
                            preferredLanguage = loggedIn.preferredLanguage,
                            deliveryInstructions = loggedIn.deliveryInstructions,
                            roleLabel = loggedIn.role.name,
                            profilePictureUrl = loggedIn.profilePictureUrl,
                            totalOrders = orders.size,
                            activePreorders = orders.count {
                                (it.status == OrderStatus.RESERVED || it.status == OrderStatus.PENDING)
                            },
                            isUpdating = updating,
                        )
                        val (completion, missing) = calculateProfileCompletion(baseContent)
                        baseContent.copy(
                            profileCompletion = completion,
                            missingFields = missing,
                        ) as BuyerProfileUiState
                    }
                SessionState.Loading -> flowOf(BuyerProfileUiState.Loading)
                is SessionState.LoggedOut -> {
                    flowOf(BuyerProfileUiState.Error("Please sign in to view your profile."))
                }
            }
        }
        .catch { emit(BuyerProfileUiState.Error(it.localizedMessage ?: "Unable to load profile")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BuyerProfileUiState.Loading)

    fun updateProfile(form: BuyerProfileFormState) {
        viewModelScope.launch {
            _isUpdating.value = true
            runCatching {
                updateProfileUseCase(
                    name = form.name.trim(),
                    phoneNumber = form.phoneNumber.trim(),
                    alternatePhoneNumber = form.alternatePhoneNumber.trim(),
                    address = form.address.trim(),
                    landmark = form.landmark.trim(),
                    city = form.city.trim(),
                    district = form.district.trim(),
                    state = form.state.trim(),
                    pincode = form.pincode.trim(),
                    preferredLanguage = form.preferredLanguage.trim(),
                    deliveryInstructions = form.deliveryInstructions.trim(),
                )
            }.onSuccess {
                _events.send("Buyer details updated successfully.")
            }.onFailure { error ->
                _events.send("Update failed: ${error.localizedMessage ?: "Unknown error"}")
            }
            _isUpdating.value = false
        }
    }
}

sealed interface BuyerProfileUiState {
    data object Loading : BuyerProfileUiState
    data class Error(val message: String) : BuyerProfileUiState
    data class Content(
        val userId: String,
        val name: String,
        val email: String,
        val phoneNumber: String,
        val alternatePhoneNumber: String,
        val address: String,
        val landmark: String,
        val city: String,
        val district: String,
        val state: String,
        val pincode: String,
        val preferredLanguage: String,
        val deliveryInstructions: String,
        val roleLabel: String,
        val profilePictureUrl: String?,
        val totalOrders: Int,
        val activePreorders: Int,
        val isUpdating: Boolean = false,
        val profileCompletion: Float = 0f,
        val missingFields: List<String> = emptyList(),
    ) : BuyerProfileUiState
}

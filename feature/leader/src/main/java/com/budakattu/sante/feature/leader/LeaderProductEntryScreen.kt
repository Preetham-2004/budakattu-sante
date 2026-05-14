package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.LeaderScaffold
import com.budakattu.sante.domain.model.ProductAvailability

@Composable
fun LeaderProductEntryRoute(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: LeaderProductFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
            if (message.contains("added", ignoreCase = true) || message.contains("updated", ignoreCase = true)) {
                onSuccess()
            }
        }
    }

    LeaderProductEntryScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNameChange = viewModel::updateName,
        onCategoryChange = viewModel::updateCategory,
        onDescriptionChange = viewModel::updateDescription,
        onAudioDescriptionChange = viewModel::updateAudioDescription,
        onQuantityChange = viewModel::updateQuantity,
        onUnitChange = viewModel::updateUnit,
        onPriceChange = viewModel::updatePrice,
        onMspChange = viewModel::updateMsp,
        onSeasonChange = viewModel::updateSeason,
        onFamilyNameChange = viewModel::updateFamilyName,
        onVillageChange = viewModel::updateVillage,
        onImageUrlChange = viewModel::updateImageUrl,
        onExpectedDispatchChange = viewModel::updateExpectedDispatch,
        onPrebookLimitChange = viewModel::updatePrebookLimit,
        onAvailabilityChange = viewModel::updateAvailability,
        onPrebookEnabledChange = viewModel::updatePrebookEnabled,
        onAutofillAudio = viewModel::autofillAudioDescription,
        onHarvestDateChange = viewModel::updateHarvestDate,
        onExpiryDateChange = viewModel::updateExpiryDate,
        onBatchNumberChange = viewModel::updateBatchNumber,
        onVisibilityChange = viewModel::updateIsVisible,
        onNotifyBuyersChange = viewModel::updateNotifyBuyers,
        onSaveDraft = viewModel::saveDraft,
        onSave = viewModel::submit,
    )
}

@Composable
private fun LeaderProductEntryScreen(
    uiState: LeaderProductFormUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAudioDescriptionChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onMspChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onFamilyNameChange: (String) -> Unit,
    onVillageChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onExpectedDispatchChange: (String) -> Unit,
    onPrebookLimitChange: (String) -> Unit,
    onAvailabilityChange: (ProductAvailability) -> Unit,
    onPrebookEnabledChange: (Boolean) -> Unit,
    onAutofillAudio: () -> Unit,
    onHarvestDateChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onBatchNumberChange: (String) -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    onNotifyBuyersChange: (Boolean) -> Unit,
    onSaveDraft: () -> Unit,
    onSave: () -> Unit,
) {
    LeaderScaffold(
        title = if (uiState.isEditMode) "Edit Listing" else "Traditional Listing",
        subtitle = "Formal registration of forest produce and logistics.",
        showBack = true,
        onBack = onBack,
    ) { outerPadding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onSaveDraft,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        ) {
                            Text("SAVE DRAFT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1.5f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            enabled = !uiState.isSaving,
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (uiState.isEditMode) "UPDATE LISTING" else "PUBLISH TO MARKET", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding)
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    ManagementFormSection(title = "Stock Configuration") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProductAvailability.entries.forEach { option ->
                                val selected = uiState.availability == option
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onAvailabilityChange(option) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = option.name.replace("_", "\n"),
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable Pre-booking", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Allows buyers to reserve upcoming harvests.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Switch(
                                checked = uiState.isPrebookEnabled,
                                onCheckedChange = onPrebookEnabledChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                item {
                    ManagementFormSection(title = "Primary Details") {
                        ManagementTextField("Product Name", uiState.name, onNameChange, "e.g. Wild Forest Honey")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Honey", "Bamboo", "Herbal", "Spice").forEach { cat ->
                                val selected = uiState.categoryName == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { onCategoryChange(cat) },
                                    label = { Text(cat) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                            }
                        }
                        
                        ManagementTextField("Description", uiState.description, onDescriptionChange, "Details for the catalog...", minLines = 3)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Accessibility Audio", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            TextButton(onClick = onAutofillAudio) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GENERATE SCRIPT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        ManagementTextField("", uiState.audioDescription, onAudioDescriptionChange, "Spoken description script...", minLines = 2)
                    }
                }

                item {
                    ManagementFormSection(title = "Commercial Value") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) { ManagementTextField("Price (₹)", uiState.price, onPriceChange, "0") }
                            Box(modifier = Modifier.weight(1f)) { ManagementTextField("Initial Qty", uiState.quantity, onQuantityChange, "0") }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) { ManagementTextField("MSP Floor (₹)", uiState.msp, onMspChange, "0") }
                            Box(modifier = Modifier.weight(1f)) { ManagementTextField("Unit", uiState.unit, onUnitChange, "kg") }
                        }
                    }
                }

                item {
                    ManagementFormSection(title = "Origin & Logistics") {
                        ManagementTextField("Supplied by Family", uiState.familyName, onFamilyNameChange, "Family ID or Name")
                        ManagementTextField("Village of Origin", uiState.village, onVillageChange, "Location")
                        ManagementTextField("Harvest Season", uiState.season, onSeasonChange, "e.g. Summer 2026")
                        ManagementTextField("Expected Dispatch", uiState.expectedDispatch, onExpectedDispatchChange, "Month/Year")
                        ManagementTextField("Pre-book Limit", uiState.prebookLimit, onPrebookLimitChange, "0")
                        ManagementTextField("Batch Number", uiState.batchNumber, onBatchNumberChange, "COOP-XXXX")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        ManagementTextField("Harvest Date", uiState.harvestDate, onHarvestDateChange, "DD/MM/YYYY")
                        ManagementTextField("Expiry Date", uiState.expiryDate, onExpiryDateChange, "DD/MM/YYYY")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.isVisible, 
                                onCheckedChange = onVisibilityChange,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text("Make visible in marketplace immediately", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.notifyBuyers, 
                                onCheckedChange = onNotifyBuyersChange,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text("Notify interested buyers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                
                item {
                    ManagementFormSection(title = "Visuals") {
                        ManagementTextField("Image URL", uiState.imageUrl, onImageUrlChange, "https://...")
                        if (uiState.imageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                AsyncImage(
                                    model = uiState.imageUrl,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManagementFormSection(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ManagementTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        if (label.isNotEmpty()) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall) },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            minLines = minLines
        )
    }
}

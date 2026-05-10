package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.theme.*
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
            if (message.contains("added", ignoreCase = true)) {
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
    HeritageScaffold(
        title = "Traditional Listing",
        subtitle = "A subtle approach to marketplace management.",
        topBarContent = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    ) { outerPadding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Surface(
                    color = TraditionalSurface,
                    tonalElevation = 4.dp,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, TraditionalPrimary.copy(alpha = 0.04f))
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = onSave,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TraditionalPrimary),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PUBLISH LISTING", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onSaveDraft,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, TraditionalPrimary.copy(alpha = 0.25f))
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = TraditionalPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE AS DRAFT", color = TraditionalPrimary, fontWeight = FontWeight.Bold)
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
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    TraditionalSection(title = "Availability") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProductAvailability.entries.forEach { option ->
                                val selected = uiState.availability == option
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onAvailabilityChange(option) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) TraditionalPrimary.copy(alpha = 0.04f) else Color.White,
                                    border = BorderStroke(1.dp, if (selected) TraditionalPrimary.copy(alpha = 0.6f) else ClayBorder.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = option.name.replace("_", "\n"),
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        color = if (selected) TraditionalPrimary else Color.Gray,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        lineHeight = 12.sp,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pre-booking enabled", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CharcoalInk)
                                Text("Reserve stock before harvest.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Switch(
                                checked = uiState.isPrebookEnabled,
                                onCheckedChange = onPrebookEnabledChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TraditionalPrimary,
                                    checkedTrackColor = TraditionalPrimary.copy(alpha = 0.2f),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                item {
                    TraditionalSection(title = "Information") {
                        SubtleTextField("Product Name", uiState.name, onNameChange, "Wild Honey")
                        
                        Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StoneWarm, modifier = Modifier.padding(top = 16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Honey", "Bamboo", "Herbal", "Other").forEach { cat ->
                                val selected = uiState.categoryName == cat
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onCategoryChange(cat) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) TraditionalSecondary.copy(alpha = 0.04f) else Color.White,
                                    border = BorderStroke(1.dp, if (selected) TraditionalSecondary.copy(alpha = 0.6f) else ClayBorder.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = cat,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        color = if (selected) TraditionalSecondary else Color.Gray,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        
                        SubtleTextField("Short Description", uiState.description, onDescriptionChange, "Describe the product...", minLines = 3)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Audio Script", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StoneWarm)
                            TextButton(onClick = onAutofillAudio) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = TraditionalPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Generate", style = MaterialTheme.typography.labelSmall, color = TraditionalPrimary)
                            }
                        }
                        SubtleTextField("", uiState.audioDescription, onAudioDescriptionChange, "The spoken description...", minLines = 2)
                    }
                }

                item {
                    TraditionalSection(title = "Value & Roots") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) { SubtleTextField("Price (₹)", uiState.price, onPriceChange, "450") }
                            Box(modifier = Modifier.weight(1f)) { SubtleTextField("Quantity", uiState.quantity, onQuantityChange, "10") }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) { SubtleTextField("MSP (₹)", uiState.msp, onMspChange, "250") }
                            Box(modifier = Modifier.weight(1f)) { SubtleTextField("Unit", uiState.unit, onUnitChange, "kg") }
                        }
                        
                        SubtleTextField("Harvest Season", uiState.season, onSeasonChange, "Summer 2026")
                        SubtleTextField("Supplied by Family", uiState.familyName, onFamilyNameChange, "Koliya Beta Family")
                        SubtleTextField("Origin Village", uiState.village, onVillageChange, "Agumbe, Shimoga")
                    }
                }

                item {
                    TraditionalSection(title = "Logistics") {
                        TraditionalTextField("Expected Dispatch", uiState.expectedDispatch, onExpectedDispatchChange, "June 2026")
                        TraditionalTextField("Pre-book Limit", uiState.prebookLimit, onPrebookLimitChange, "10")
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TraditionalTextField("Harvest Date", uiState.harvestDate, onHarvestDateChange, "25 May 2026")
                        TraditionalTextField("Expiry Date", uiState.expiryDate, onExpiryDateChange, "25 Nov 2026")
                        TraditionalTextField("Batch Number", uiState.batchNumber, onBatchNumberChange, "HB-2026-001")
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                            Checkbox(checked = uiState.isVisible, onCheckedChange = onVisibilityChange, colors = CheckboxDefaults.colors(checkedColor = TraditionalPrimary))
                            Text("Visible in market", style = MaterialTheme.typography.bodySmall, color = CharcoalInk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TraditionalTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    SubtleTextField(label, value, onValueChange, placeholder)
}

@Composable
private fun TraditionalSection(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = TraditionalSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, ClayBorder.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StoneWarm, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SubtleTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        if (label.isNotEmpty()) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = StoneWarm)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            placeholder = { Text(placeholder, color = Color.LightGray, style = MaterialTheme.typography.bodySmall) },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TraditionalPrimary,
                unfocusedBorderColor = ClayBorder.copy(alpha = 0.5f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            minLines = minLines
        )
    }
}

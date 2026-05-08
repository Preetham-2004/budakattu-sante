package com.budakattu.sante.feature.leader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.domain.model.ProductAvailability

private val LeaderCategories = listOf("Honey", "Bamboo Crafts", "Herbal Produce", "Others")

@Composable
fun LeaderProductEntryRoute(
    onBack: () -> Unit,
    viewModel: LeaderProductFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
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
    onSave: () -> Unit,
) {
    HeritageScaffold(
        title = "Add Marketplace Product",
        subtitle = "Leaders can publish a product once, add accessibility support, and open pre-booking before production is complete.",
    ) { outerPadding ->
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Leader Product Form", style = MaterialTheme.typography.headlineSmall)
                        OutlinedButton(onClick = onBack) {
                            Text("Back")
                        }
                    }
                }
                item {
                    ForestCard {
                        Text("Availability mode", style = MaterialTheme.typography.titleLarge)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ProductAvailability.entries.forEach { option ->
                                FilterChip(
                                    selected = uiState.availability == option,
                                    onClick = { onAvailabilityChange(option) },
                                    label = { Text(option.name.replace("_", " ")) },
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable pre-booking", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Turn this on when buyers should reserve before harvest or manufacturing is complete.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Switch(
                                checked = uiState.isPrebookEnabled,
                                onCheckedChange = onPrebookEnabledChange,
                            )
                        }
                    }
                }
                item {
                    ForestCard {
                        Text("Product details", style = MaterialTheme.typography.titleLarge)
                        LeaderProductTextField("Product name", uiState.name, onNameChange)
                        CategoryChips(
                            selectedCategory = uiState.categoryName,
                            onCategoryChange = onCategoryChange,
                        )
                        LeaderProductTextField("Description", uiState.description, onDescriptionChange, minLines = 4)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Audio description", style = MaterialTheme.typography.titleMedium)
                            OutlinedButton(onClick = onAutofillAudio) {
                                Text("Use description")
                            }
                        }
                        LeaderProductTextField(
                            label = "Audio description script",
                            value = uiState.audioDescription,
                            onValueChange = onAudioDescriptionChange,
                            minLines = 3,
                        )
                        LeaderProductTextField("Image URL", uiState.imageUrl, onImageUrlChange)
                    }
                }
                item {
                    ForestCard {
                        Text("Pricing and stock", style = MaterialTheme.typography.titleLarge)
                        LeaderProductTextField("Quantity", uiState.quantity, onQuantityChange)
                        LeaderProductTextField("Unit", uiState.unit, onUnitChange)
                        LeaderProductTextField("Price per unit", uiState.price, onPriceChange)
                        LeaderProductTextField("MSP per unit", uiState.msp, onMspChange)
                        LeaderProductTextField("Season", uiState.season, onSeasonChange)
                        if (uiState.isPrebookEnabled) {
                            LeaderProductTextField("Expected dispatch", uiState.expectedDispatch, onExpectedDispatchChange)
                            LeaderProductTextField("Maximum pre-book quantity", uiState.prebookLimit, onPrebookLimitChange)
                        }
                    }
                }
                item {
                    ForestCard {
                        Text("Source and traceability", style = MaterialTheme.typography.titleLarge)
                        LeaderProductTextField("Family or collective", uiState.familyName, onFamilyNameChange)
                        LeaderProductTextField("Village", uiState.village, onVillageChange)
                    }
                }
                item {
                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSave,
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(vertical = 2.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Publish product")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderProductTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        minLines = minLines,
    )
}

@Composable
private fun CategoryChips(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text("Category", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LeaderCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category) },
                )
            }
        }
    }
}

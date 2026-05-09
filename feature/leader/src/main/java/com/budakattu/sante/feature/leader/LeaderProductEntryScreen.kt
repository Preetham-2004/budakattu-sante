package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.theme.*
import com.budakattu.sante.domain.model.ProductAvailability

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
        onHarvestDateChange = viewModel::updateHarvestDate,
        onExpiryDateChange = viewModel::updateExpiryDate,
        onBatchNumberChange = viewModel::updateBatchNumber,
        onVisibilityChange = viewModel::updateIsVisible,
        onNotifyBuyersChange = viewModel::updateNotifyBuyers,
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
    onSave: () -> Unit,
) {
    HeritageScaffold(
        title = "Add Marketplace Product",
        subtitle = "Leaders can publish a product once, add accessibility support, and open pre-booking before production is complete.",
    ) { outerPadding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Surface(
                    color = Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = onSave,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publish Product", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { /* Save as Draft */ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ForestPrimary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = ForestPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save as Draft", color = ForestPrimary)
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
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Leader Product Form", style = MaterialTheme.typography.titleLarge)
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    }
                }

                // Section 1: Availability Mode
                item {
                    ForestCard {
                        SectionHeader("Availability mode")
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProductAvailability.entries.forEach { option ->
                                val selected = uiState.availability == option
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onAvailabilityChange(option) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) SoftMoss else Color.White,
                                    border = BorderStroke(1.dp, if (selected) ForestPrimary else Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = option.name.replace("_", " "),
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        color = if (selected) ForestPrimary else Color.Gray,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable pre-booking", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Turn this on when buyers should reserve before harvest or manufacturing is complete.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = uiState.isPrebookEnabled,
                                onCheckedChange = onPrebookEnabledChange,
                                colors = SwitchDefaults.colors(checkedThumbColor = ForestPrimary, checkedTrackColor = ForestLight)
                            )
                        }

                        if (uiState.isPrebookEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SanteTextField("Expected Dispatch", uiState.expectedDispatch, onExpectedDispatchChange, "June 2026")
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    SanteTextField("Pre-book Limit", uiState.prebookLimit, onPrebookLimitChange, "10", trailingIcon = "qty")
                                }
                            }
                        }
                    }
                }

                // Section 2: Product Details
                item {
                    ForestCard {
                        SectionHeader("Product details")
                        SanteTextField("Product name", uiState.name, onNameChange, "e.g., Wild Forest Honey")
                        
                        Text("Category *", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                CategoryOption("Honey", Icons.Default.BrightnessLow),
                                CategoryOption("Bamboo Crafts", Icons.Default.Architecture),
                                CategoryOption("Herbal Produce", Icons.Default.Eco),
                                CategoryOption("Other", Icons.Default.Category)
                            ).forEach { cat ->
                                val selected = uiState.categoryName == cat.name
                                Surface(
                                    modifier = Modifier.weight(1f).clickable { onCategoryChange(cat.name) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) GoldLustre.copy(alpha = 0.2f) else Color.White,
                                    border = BorderStroke(1.dp, if (selected) GoldLustre else Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(cat.icon, contentDescription = null, tint = if (selected) AmberHarvest else Color.Gray, modifier = Modifier.size(18.dp))
                                        Text(cat.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    }
                                }
                            }
                        }
                        
                        SanteTextField("Short description *", uiState.description, onDescriptionChange, "Describe your product, its origin, and benefits...", minLines = 4)
                        Text(
                            text = "${uiState.description.length} / 200",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Section 3: Product Images
                item {
                    ForestCard {
                        SectionHeader("Product images")
                        Text("Add 1 to 5 high-quality images", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        
                        LazyRow(
                            contentPadding = PaddingValues(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Surface(
                                    modifier = Modifier.size(100.dp).clickable { /* Pick Image */ },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                                        Text("Add image", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }
                            items(listOf(uiState.imageUrl).filter { it.isNotBlank() }) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        SanteTextField("Image URL", uiState.imageUrl, onImageUrlChange, "Paste link to product image")
                    }
                }

                // Section 4: Pricing & Quantity
                item {
                    ForestCard {
                        SectionHeader("Pricing & Quantity")
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SanteTextField("Price (₹) *", uiState.price, onPriceChange, "450")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SanteTextField("MSP (₹) *", uiState.msp, onMspChange, "250")
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SanteTextField("Available quantity *", uiState.quantity, onQuantityChange, "25", trailingIcon = "qty")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SanteTextField("Unit *", uiState.unit, onUnitChange, "kg")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = ForestBackground,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = ForestPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Display of Minimum Support Price (MSP)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("₹${uiState.msp} / ${uiState.unit}", style = MaterialTheme.typography.labelMedium)
                                    val isSafe = (uiState.price.toFloatOrNull() ?: 0f) >= (uiState.msp.toFloatOrNull() ?: 0f)
                                    Text(
                                        text = if (isSafe) "Your price is above the MSP. Good job!" else "Your price is below the MSP.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSafe) ForestPrimary else Color.Red
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                if ((uiState.price.toFloatOrNull() ?: 0f) >= (uiState.msp.toFloatOrNull() ?: 0f)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestPrimary)
                                }
                            }
                        }
                    }
                }

                // Section 5: Harvest / Production
                item {
                    ForestCard {
                        SectionHeader("Harvest / Production")
                        SanteTextField("Season / Harvest Window", uiState.season, onSeasonChange, "Summer 2026")
                        SanteTextField("Expected harvest date *", uiState.harvestDate, onHarvestDateChange, "25 May 2026", trailingIcon = "date")
                        SanteTextField("Production / Expiry date (if any)", uiState.expiryDate, onExpiryDateChange, "25 Nov 2026", trailingIcon = "date")
                        SanteTextField("Origin (Village / Forest Area) *", uiState.village, onVillageChange, "Agumbe, Shimoga, Karnataka")
                    }
                }

                // Section 6: Accessibility & Support
                item {
                    ForestCard {
                        SectionHeader("Accessibility & Support")
                        Text("Audio description (GenAI) *", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "We will generate an audio description for this product using AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Button(
                            onClick = onAutofillAudio,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestBackground),
                            border = BorderStroke(1.dp, ForestLight.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ForestPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate audio description", color = ForestPrimary)
                        }
                        
                        SanteTextField("Audio script", uiState.audioDescription, onAudioDescriptionChange, "The generated AI script goes here...", minLines = 3)
                    }
                }

                // Section 7: Supply Source
                item {
                    ForestCard {
                        SectionHeader("Supply Source")
                        SanteTextField("Supplied by family / Group *", uiState.familyName, onFamilyNameChange, "e.g., Koliya Beta Family Group")
                        SanteTextField("Batch / Lot number (optional)", uiState.batchNumber, onBatchNumberChange, "HB-2026-05-001")
                    }
                }

                // Section 8: Preview
                item {
                    ForestCard {
                        SectionHeader("Preview")
                        ProductPreviewCard(uiState)
                    }
                }

                // Section 9: Publish Settings
                item {
                    ForestCard {
                        SectionHeader("Publish settings")
                        Row(verticalAlignment = Alignment.Top) {
                            Checkbox(checked = uiState.isVisible, onCheckedChange = onVisibilityChange)
                            Column {
                                Text("Make product visible to buyers", fontWeight = FontWeight.Bold)
                                Text("Product will be listed in the marketplace", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Checkbox(checked = uiState.notifyBuyers, onCheckedChange = onNotifyBuyersChange)
                            Column {
                                Text("Notify buyers about new product", fontWeight = FontWeight.Bold)
                                Text("Send push notification to interested buyers", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                item {
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SanteTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    trailingIcon: String? = null
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(text = "$label *", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForestPrimary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            minLines = minLines,
            trailingIcon = {
                when (trailingIcon) {
                    "date" -> Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    "qty" -> Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        )
    }
}

@Composable
private fun ProductPreviewCard(state: LeaderProductFormUiState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = ForestBackground
            ) {
                if (state.imageUrl.isNotBlank()) {
                    AsyncImage(model = state.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.name.ifBlank { "Product Name" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = ForestBackground,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = state.availability.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ForestPrimary
                        )
                    }
                }
                
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Category", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(12.dp), tint = AmberHarvest)
                    Text(state.categoryName, style = MaterialTheme.typography.labelSmall)
                }
                
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Price", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹${state.price.ifBlank { "0" }} / ${state.unit}", fontWeight = FontWeight.Black)
                }
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Available", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${state.quantity.ifBlank { "0" }} ${state.unit}", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private data class CategoryOption(val name: String, val icon: ImageVector)

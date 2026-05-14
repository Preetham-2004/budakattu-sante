package com.budakattu.sante.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.BuyerRouteStrip
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.ForestBackground
import com.budakattu.sante.feature.catalog.viewmodel.HeritageViewModel

@Composable
fun HeritageRouteScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HeritageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HeritageScaffold(
        title = "Living Heritage",
        subtitle = "Meet the forest wisdom, the families, and the rituals.",
        showBack = true,
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BuyerRouteStrip(
                activeRoute = NavRoutes.HERITAGE,
                onNavigate = onNavigate,
                marketRoute = NavRoutes.CATALOG,
                cartRoute = NavRoutes.CART,
                profileRoute = NavRoutes.PROFILE,
            )

            // Dynamic Artisans Section
            Text(
                "Tribal Artisans & Gatherers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            
            if (uiState.families.isEmpty() && !uiState.isLoading) {
                ForestCard {
                    Text(
                        "The cooperative is currently onboarding families for digital traceability. Every batch you buy is tracked back to its source.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    items(uiState.families) { family ->
                        FamilyCircleItem(family.familyName, family.primaryCraft)
                    }
                    // Add some static ones if list is short
                    if (uiState.families.size < 3) {
                        item { FamilyCircleItem("Jenu Kuruba Collective", "Honey Extraction") }
                        item { FamilyCircleItem("Soliga Women Group", "Herbal Processing") }
                        item { FamilyCircleItem("Bandipur Fringe Circle", "Fruit Drying") }
                    }
                }
            }

            ForestCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "The Harvest Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "Every batch carries a family memory, a season mark, and a cooperative seal before it reaches the buyer. We ensure no harvest violates the forest's regeneration cycle.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Seasonal Calendar
            Text(
                "Forest Bloom Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SeasonalRow("SUMMER", "Wild Honey & Shikakai", isActive = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    SeasonalRow("MONSOON", "Forest Amla & Herbal Roots", isActive = false)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    SeasonalRow("WINTER", "Bamboo Crafts & Spices", isActive = false)
                }
            }

            ForestCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Grass, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Cultural Pillars",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "Honey follows bloom cycles. Bamboo follows moon-timed craft runs. Herbal produce follows protection rules. Our commerce respects the silence of the woods.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val familyCount = if (uiState.families.isNotEmpty()) uiState.families.size.toString() else "12"
                RouteBadge(label = "Families", value = familyCount)
                RouteBadge(label = "District", value = "BR Hills")
            }
        }
    }
}

@Composable
private fun FamilyCircleItem(name: String, craft: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp),
    ) {
        AsyncImage(
            model = "https://i.pravatar.cc/150?u=$name",
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ForestBackground),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = craft,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun SeasonalRow(season: String, products: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = season,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
            Text(products, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (isActive) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {
                Text(
                    "ACTIVE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

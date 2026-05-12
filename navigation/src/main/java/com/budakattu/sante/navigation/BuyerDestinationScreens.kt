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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.budakattu.sante.core.ui.theme.BarkBrown
import com.budakattu.sante.core.ui.theme.ForestBackground
import com.budakattu.sante.core.ui.theme.ForestPrimary
import com.budakattu.sante.core.ui.theme.AmberHarvest
import com.budakattu.sante.core.ui.theme.SunsetClay
import com.budakattu.sante.feature.catalog.viewmodel.HeritageViewModel

@Composable
fun HeritageRouteScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HeritageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HeritageScaffold(
        title = "Living Heritage",
        subtitle = "Meet the forest wisdom, the families, and the rituals.",
        showBack = true,
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                color = BarkBrown
            )
            
            if (uiState.families.isEmpty() && !uiState.isLoading) {
                ForestCard {
                    Text("The cooperative is currently onboarding families for digital traceability. Every batch you buy is tracked back to its source.", color = Color.Gray)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
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
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberHarvest)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("The Harvest Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Every batch carries a family memory, a season mark, and a cooperative seal before it reaches the buyer. We ensure no harvest violates the forest's regeneration cycle.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Seasonal Calendar
            Text(
                "Forest Bloom Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BarkBrown
            )
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ForestPrimary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SeasonalRow("SUMMER", "Wild Honey & Shikakai", true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ForestBackground)
                    SeasonalRow("MONSOON", "Forest Amla & Herbal Roots", false)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ForestBackground)
                    SeasonalRow("WINTER", "Bamboo Crafts & Spices", false)
                }
            }

            ForestCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Grass, contentDescription = null, tint = ForestPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Cultural Pillars", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Honey follows bloom cycles. Bamboo follows moon-timed craft runs. Herbal produce follows protection rules. Our commerce respects the silence of the woods.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
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
        modifier = Modifier.width(100.dp)
    ) {
        AsyncImage(
            model = "https://i.pravatar.cc/150?u=$name",
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ForestBackground),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = craft,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SeasonalRow(season: String, products: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(season, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isActive) ForestPrimary else Color.Gray)
            Text(products, style = MaterialTheme.typography.bodyMedium, color = BarkBrown)
        }
        if (isActive) {
            Surface(
                color = ForestPrimary,
                shape = CircleShape
            ) {
                Text(
                    "ACTIVE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OrdersRouteScreen(
    onNavigate: (String) -> Unit,
) {
    HeritageScaffold(
        title = "Order Drumbeat",
        subtitle = "Track buyer intent, cooperative preparation, and fulfilment milestones with a stronger operational view.",
    ) { padding ->
        DestinationBody(
            padding = padding,
            activeRoute = NavRoutes.ORDERS,
            onNavigate = onNavigate,
        ) {
            ForestCard {
                Text("Current queue", style = MaterialTheme.typography.titleLarge)
                Text("3 preorder requests are waiting for stock confirmation and family batch assignment.")
            }
            ForestCard {
                Text("Expected movement", style = MaterialTheme.typography.titleLarge)
                Text("Honey and herbal produce are moving this week. Craft orders are grouped into a weekend dispatch cycle.")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                RouteBadge(label = "Open", value = "03")
                RouteBadge(label = "Fulfilment", value = "05 Days")
            }
        }
    }
}

@Composable
fun ProfileRouteScreen(
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
) {
    HeritageScaffold(
        title = "Buyer Profile",
        subtitle = "Control your access and preferences.",
        showBack = true,
        onBack = onBack
    ) { padding ->
        DestinationBody(
            padding = padding,
            activeRoute = NavRoutes.PROFILE,
            onNavigate = onNavigate,
        ) {
            ForestCard {
                Text("Trust posture", style = MaterialTheme.typography.titleLarge)
                Text("Authenticated buyer account with Google and Firebase session persistence enabled.")
            }
            ForestCard {
                Text("Preferences", style = MaterialTheme.typography.titleLarge)
                Text("Preferred language, delivery region, and accessibility settings can be surfaced here next.")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut,
            ) {
                Text("Sign out")
            }
        }
    }
}

@Composable
private fun DestinationBody(
    padding: PaddingValues,
    activeRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = {
        BuyerRouteStrip(
            activeRoute = activeRoute,
            onNavigate = onNavigate,
            marketRoute = NavRoutes.CATALOG,
            cartRoute = NavRoutes.CART,
            profileRoute = NavRoutes.PROFILE,
        )
            content()
        },
    )
}

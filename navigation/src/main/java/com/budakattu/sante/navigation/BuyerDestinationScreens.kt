package com.budakattu.sante.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.components.BuyerRouteStrip
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge

@Composable
fun HeritageRouteScreen(
    onNavigate: (String) -> Unit,
) {
    HeritageScaffold(
        title = "Living Heritage",
        subtitle = "Meet the forest wisdom, the families, and the rituals that give each product a lineage.",
    ) { padding ->
        DestinationBody(
            padding = padding,
            activeRoute = NavRoutes.HERITAGE,
            onNavigate = onNavigate,
        ) {
            ForestCard {
                Text("The harvest code", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Every batch carries a family memory, a season mark, and a cooperative seal before it reaches the buyer.",
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            ForestCard {
                Text("Cultural pillars", style = MaterialTheme.typography.titleLarge)
                Text("Honey follows bloom cycles. Bamboo follows moon-timed craft runs. Herbal produce follows protection rules.")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                RouteBadge(label = "Families", value = "12")
                RouteBadge(label = "District", value = "BR Hills")
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
) {
    HeritageScaffold(
        title = "Buyer Profile",
        subtitle = "Control your access, preferences, and marketplace posture from one strong command point.",
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
                heritageRoute = NavRoutes.HERITAGE,
                ordersRoute = NavRoutes.ORDERS,
                profileRoute = NavRoutes.PROFILE,
            )
            content()
        },
    )
}

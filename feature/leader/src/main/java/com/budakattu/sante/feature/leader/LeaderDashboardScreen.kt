package com.budakattu.sante.feature.leader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.HighlightCard
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.*

@Composable
fun LeaderDashboardScreen(
    leaderName: String,
    leaderId: String,
    leaderRoleLabel: String,
    onAddProduct: () -> Unit,
    onOpenOrders: () -> Unit,
    onSignOut: () -> Unit,
) {
    HeritageScaffold(
        title = "Command",
        subtitle = "Overseeing the marketplace with roots and discipline.",
    ) { innerPadding: PaddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Hello, $leaderName", style = MaterialTheme.typography.headlineMedium)
                        Text(text = leaderRoleLabel, color = ForestMedium)
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.clip(CircleShape).background(Color.White)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ForestPrimary)
                    }
                }
            }

            item {
                HighlightCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RouteBadge(label = "Orders", value = "07", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        RouteBadge(label = "Alerts", value = "02", color = SunsetClay, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DashboardActionTile(
                        title = "Add Product",
                        icon = Icons.Default.Add,
                        color = ForestPrimary,
                        onClick = onAddProduct,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardActionTile(
                        title = "Orders",
                        icon = Icons.Default.ShoppingCart,
                        color = ForestMedium,
                        onClick = onOpenOrders,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                DashboardActionTile(
                    title = "Inventory",
                    icon = Icons.Default.Inventory,
                    color = DeepSlate,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ForestCard {
                    Text("Market Intelligence", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("The demand for Wild Honey is increasing. Ensure all families have their supply logs updated.")
                }
            }
        }
    }
}

@Composable
private fun DashboardActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.1f), CircleShape).padding(6.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

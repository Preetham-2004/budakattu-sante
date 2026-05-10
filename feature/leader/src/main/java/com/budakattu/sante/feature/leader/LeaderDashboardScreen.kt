package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.*
import com.budakattu.sante.domain.model.Product

@Composable
fun LeaderDashboardRoute(
    onAddProduct: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenInsights: () -> Unit,
    onEditDraft: (String) -> Unit,
    onSignOut: () -> Unit,
    viewModel: LeaderDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LeaderDashboardScreen(
        uiState = uiState,
        onAddProduct = onAddProduct,
        onOpenOrders = onOpenOrders,
        onOpenInventory = onOpenInventory,
        onOpenInsights = onOpenInsights,
        onEditDraft = onEditDraft,
        onSignOut = onSignOut
    )
}

@Composable
private fun LeaderDashboardScreen(
    uiState: LeaderDashboardUiState,
    onAddProduct: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenInsights: () -> Unit,
    onEditDraft: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    HeritageScaffold(
        title = "Command Center",
        subtitle = "Cooperative management with rooted trade identity.",
        topBarContent = {
            if (uiState is LeaderDashboardUiState.Success) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = uiState.profilePictureUrl ?: "https://i.pravatar.cc/150?u=${uiState.leaderName}",
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Namaste, ${uiState.leaderName}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(LeafAccent.copy(alpha = 0.7f), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "System Sync Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    Surface(
                        onClick = onSignOut,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(height = 52.dp, width = 68.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("Logout", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding: PaddingValues ->
        when (uiState) {
            is LeaderDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TraditionalPrimary, strokeWidth = 2.dp)
                }
            }
            is LeaderDashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = TraditionalPrimary.copy(alpha = 0.7f))
                }
            }
            is LeaderDashboardUiState.Success -> {
                LeaderDashboardContent(
                    data = uiState,
                    innerPadding = innerPadding,
                    onAddProduct = onAddProduct,
                    onOpenOrders = onOpenOrders,
                    onOpenInventory = onOpenInventory,
                    onOpenInsights = onOpenInsights,
                    onEditDraft = onEditDraft
                )
            }
        }
    }
}

@Composable
private fun LeaderDashboardContent(
    data: LeaderDashboardUiState.Success,
    innerPadding: PaddingValues,
    onAddProduct: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenInsights: () -> Unit,
    onEditDraft: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SummaryMetrics(data)
        }

        item {
            OperationsSection(onAddProduct, onOpenOrders, onOpenInventory)
        }

        item {
            AnalyticsInsightsStrip(onClick = onOpenInsights)
        }

        if (data.drafts.isNotEmpty()) {
            item {
                SavedDraftsSection(data.drafts, onEditDraft)
            }
        }

        item {
            EarthyMarketInsight()
        }
    }
}

@Composable
private fun SummaryMetrics(data: LeaderDashboardUiState.Success) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val metricModifier = Modifier.weight(1f).fillMaxHeight()
        MetricCard("Orders", data.pendingOrdersCount, "Pending", Icons.Default.ShoppingCart, TraditionalPrimary, metricModifier)
        MetricCard("Alerts", data.alertsCount, "Attention", Icons.Default.Notifications, SunsetClay, metricModifier)
        MetricCard("Status", "Active", "Verified", Icons.Default.CheckCircle, TraditionalSecondary, metricModifier)
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    subLabel: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = TraditionalSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp, letterSpacing = 1.sp)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CharcoalInk.copy(alpha = 0.9f))
            Text(text = subLabel, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f), fontSize = 8.sp)
        }
    }
}

@Composable
private fun OperationsSection(
    onAddProduct: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenInventory: () -> Unit
) {
    Column {
        Text("Operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BarkBrown)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OperationCard(
                title = "Add Product",
                desc = "List new produce",
                icon = Icons.Default.Add,
                onClick = onAddProduct,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            OperationCard(
                title = "Manage Orders",
                desc = "Customer requests",
                icon = Icons.Default.Storefront,
                onClick = onOpenOrders,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OperationCard(
            title = "Inventory & Stock",
            desc = "Track and manage cooperative stock",
            icon = Icons.Default.Inventory2,
            onClick = onOpenInventory,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OperationCard(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = TraditionalSurface,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, ClayBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).background(TraditionalBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = TraditionalPrimary.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                    contentDescription = null, 
                    tint = TraditionalPrimary.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CharcoalInk)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun AnalyticsInsightsStrip(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TraditionalBackground,
        border = BorderStroke(1.dp, ClayBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = AmberHarvest.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Performance Insights", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = BarkBrown)
                Text("Monitor marketplace trends", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = AmberHarvest.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun SavedDraftsSection(drafts: List<Product>, onEditDraft: (String) -> Unit) {
    Column {
        Text("Saved Drafts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BarkBrown)
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            drafts.take(2).forEach { draft ->
                DraftCardItem(draft, onEditDraft)
            }
        }
    }
}

@Composable
private fun DraftCardItem(product: Product, onEditDraft: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TraditionalSurface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = TraditionalBackground
            ) {
                AsyncImage(
                    model = product.imageUrls.firstOrNull(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CharcoalInk)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Draft", style = MaterialTheme.typography.labelSmall, color = AmberHarvest, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Last edited recently", style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 9.sp)
                }
            }
            
            IconButton(
                onClick = { onEditDraft(product.productId) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TraditionalPrimary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EarthyMarketInsight() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = TraditionalSecondary.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, TraditionalSecondary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TraditionalSecondary.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Market Pulse", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TraditionalSecondary)
                Text(
                    text = "Honey interest is rising in nearby clusters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalInk.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

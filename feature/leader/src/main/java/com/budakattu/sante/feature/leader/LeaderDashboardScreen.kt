package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    onEditDraft: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    HeritageScaffold(
        title = "Command Center",
        subtitle = "A traditional approach to modern marketplace management.",
        topBarContent = {
            if (uiState is LeaderDashboardUiState.Success) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://i.pravatar.cc/150?u=${uiState.leaderName}",
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Namaste, ${uiState.leaderName}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(LeafAccent, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sync Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    Surface(
                        onClick = onSignOut,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(height = 56.dp, width = 72.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Logout", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding: PaddingValues ->
        when (uiState) {
            is LeaderDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TraditionalPrimary)
                }
            }
            is LeaderDashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is LeaderDashboardUiState.Success -> {
                LeaderDashboardContent(
                    data = uiState,
                    innerPadding = innerPadding,
                    onAddProduct = onAddProduct,
                    onOpenOrders = onOpenOrders,
                    onOpenInventory = onOpenInventory,
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
    onEditDraft: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SummaryMetrics(data)
        }

        item {
            OperationsSection(onAddProduct, onOpenOrders, onOpenInventory)
        }

        item {
            InsightsStrip()
        }

        if (data.drafts.isNotEmpty()) {
            item {
                SavedDraftsSection(data.drafts, onEditDraft)
            }
        }

        item {
            AiInsightCard()
        }
    }
}

@Composable
private fun SummaryMetrics(data: LeaderDashboardUiState.Success) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            label = "Orders",
            value = data.pendingOrdersCount,
            subLabel = "Pending",
            icon = Icons.Default.ShoppingCart,
            color = ForestPrimary,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "Alerts",
            value = data.alertsCount,
            subLabel = "Attention",
            icon = Icons.Default.Notifications,
            color = SunsetClay,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "Status",
            value = "Active",
            subLabel = "Systems Go",
            icon = Icons.Default.CheckCircle,
            color = LeafAccent,
            modifier = Modifier.weight(1f)
        )
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
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
            Text(text = subLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.8f), fontSize = 8.sp)
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
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OperationCard(
                title = "Add Product",
                desc = "List new products to marketplace",
                icon = Icons.Default.Add,
                onClick = onAddProduct,
                modifier = Modifier.weight(1f)
            )
            OperationCard(
                title = "Manage Orders",
                desc = "View and manage customer orders",
                icon = Icons.Default.Storefront,
                onClick = onOpenOrders,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OperationCard(
            title = "Inventory & Stock",
            desc = "Track stock and manage inventory",
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
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SoftMoss, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ForestPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BarkBrown)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = Color.Gray, lineHeight = 14.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .size(24.dp)
                    .background(ForestPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun InsightsStrip() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AmberHarvest.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, AmberHarvest.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AmberHarvest.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = AmberHarvest, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Analytics & Insights", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BarkBrown)
                Text("Track performance and marketplace trends", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Surface(
                onClick = {},
                color = AmberHarvest.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Open Dashboard", style = MaterialTheme.typography.labelSmall, color = AmberHarvest, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AmberHarvest, modifier = Modifier.size(14.dp))
                }
            }
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
                DraftCard(draft, onEditDraft)
            }
        }
    }
}

@Composable
private fun DraftCard(product: Product, onEditDraft: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrls.firstOrNull(),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftMoss),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BarkBrown)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = GoldLustre.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Draft", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = AmberHarvest, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• 80% complete", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Edited 2 hours ago", style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontSize = 8.sp)
            }
            
            IconButton(
                onClick = { onEditDraft(product.productId) },
                modifier = Modifier.size(32.dp).background(TraditionalBackground, CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = ForestPrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AiInsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LeafAccent.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, LeafAccent.copy(alpha = 0.2f))
    ) {
        Box {
            // Chart Background Placeholder
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = LeafAccent.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
            )
            
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LeafAccent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("AI Market Insight", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ForestPrimary)
                    Text(
                        text = "Honey demand increased by 32% in Bangalore this week.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalInk.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Recommended: Increase harvest allocation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

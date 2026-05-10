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
import com.budakattu.sante.core.ui.components.LeaderScaffold
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
    LeaderScaffold(
        title = "Cooperative HQ",
        subtitle = "Professional management of tribal harvests & trade.",
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
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = uiState.leaderName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Administrator",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        when (uiState) {
            is LeaderDashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LeaderPrimary, strokeWidth = 2.dp)
                }
            }
            is LeaderDashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = LeaderError)
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            SummaryMetrics(data)
        }

        item {
            OperationsGrid(onAddProduct, onOpenOrders, onOpenInventory)
        }

        item {
            InsightsCard(onClick = onOpenInsights)
        }

        if (data.drafts.isNotEmpty()) {
            item {
                DraftsSection(data.drafts, onEditDraft)
            }
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
        MetricCard("Active Orders", data.pendingOrdersCount, Icons.Default.Inventory, LeaderPrimary, metricModifier)
        MetricCard("Pending Alerts", data.alertsCount, Icons.Default.NotificationsActive, LeaderError, metricModifier)
        MetricCard("Efficiency", "94%", Icons.AutoMirrored.Filled.TrendingUp, LeaderSecondary, metricModifier)
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = LeaderSurface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = CharcoalInk)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun OperationsGrid(
    onAddProduct: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenInventory: () -> Unit
) {
    Column {
        Text("Primary Operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = LeaderPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OperationActionCard(
                title = "List Produce",
                icon = Icons.Default.PostAdd,
                onClick = onAddProduct,
                color = LeaderPrimary,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            OperationActionCard(
                title = "Order Logs",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                onClick = onOpenOrders,
                color = LeaderSecondary,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OperationActionCard(
            title = "Inventory Warehouse",
            icon = Icons.Default.Warehouse,
            onClick = onOpenInventory,
            color = LeaderSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OperationActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color,
        contentColor = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun InsightsCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LeaderHighlight,
        border = BorderStroke(1.dp, LeaderSecondary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Insights, contentDescription = null, tint = LeaderPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Cooperative Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LeaderPrimary)
                Text("Review demand forecasting and harvest data", style = MaterialTheme.typography.labelSmall, color = LeaderSecondary.copy(alpha = 0.7f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = LeaderPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DraftsSection(drafts: List<Product>, onEditDraft: (String) -> Unit) {
    Column {
        Text("Pending Drafts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = LeaderPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            drafts.forEach { draft ->
                DraftItem(draft, onEditDraft)
            }
        }
    }
}

@Composable
private fun DraftItem(product: Product, onEditDraft: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = LeaderSurface,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(LeaderBackground)) {
                AsyncImage(model = product.imageUrls.firstOrNull(), contentDescription = null, contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CharcoalInk)
                Text("Last saved: ${product.lastModifiedAt}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            TextButton(onClick = { onEditDraft(product.productId) }) {
                Text("RESUME", fontWeight = FontWeight.Bold, color = LeaderPrimary)
            }
        }
    }
}

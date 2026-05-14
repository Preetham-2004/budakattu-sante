package com.budakattu.sante.feature.leader

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.*
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
    viewModel: LeaderDashboardViewModel = hiltViewModel(),
    chatViewModel: SupportChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chatMessages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    var showChat by remember { mutableStateOf(value = false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { text ->
                chatViewModel.sendMessage(text)
            }
        }
    }

    Box {
        LeaderDashboardScreen(
            uiState = uiState,
            onAddProduct = onAddProduct,
            onOpenOrders = onOpenOrders,
            onOpenInventory = onOpenInventory,
            onOpenInsights = onOpenInsights,
            onEditDraft = onEditDraft,
            onSignOut = onSignOut,
        ) { showChat = true }

        if (showChat) {
            SupportChatBottomSheet(
                onDismissRequest = { showChat = false },
                onSendMessage = chatViewModel::sendMessage,
                onVoiceInputClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                    }
                    speechLauncher.launch(intent)
                },
                messages = chatMessages,
                isTyping = isTyping
            )
        }
    }
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
    onOpenChat: () -> Unit,
) {
    LeaderScaffold(
        title = "Cooperative HQ",
        subtitle = "Professional management of tribal harvests & trade.",
        floatingActionButton = {
            SupportChatFAB(onClick = onOpenChat)
        },
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
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = uiState.leaderName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Administrator",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    AudioGuidanceButton(
                        text = "This is your Cooperative Headquarters. You can list new produce, manage inventory, and view order logs.",
                        backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun InsightsCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Cooperative Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                Text("Review demand forecasting and harvest data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
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
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                AsyncImage(model = product.imageUrls.firstOrNull(), contentDescription = null, contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Last saved: ${product.lastModifiedAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            TextButton(onClick = { onEditDraft(product.productId) }) {
                Text("RESUME", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

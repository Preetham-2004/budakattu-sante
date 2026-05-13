package com.budakattu.sante.feature.leader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.LeaderScaffold
import com.budakattu.sante.core.ui.theme.*
import com.budakattu.sante.domain.model.Product

// Earthy, fresh theme colors for the inventory screen
private val CardBackground = Color(0xFFFEF7E8)      // Warm off-white
private val AccentForest = Color(0xFF2D6A4F)        // Deep forest green
private val AccentWarm = Color(0xFFE9C46A)          // Golden sand
private val WarningRed = Color(0xFFE63946)           // Alert red
private val LowStockBg = Color(0xFFFFE5E5)           // Soft red tint
private val TextDark = Color(0xFF2F3E46)             // Dark slate
private val TextMuted = Color(0xFF6C757D)            // Muted grey
private val DividerColor = Color(0xFFEAE0D5)         // Soft beige divider

@Composable
fun LeaderInventoryRoute(
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    viewModel: LeaderInventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LeaderInventoryScreen(
        uiState = uiState,
        onBack = onBack,
        onAddProduct = onAddProduct
    )
}

@Composable
private fun LeaderInventoryScreen(
    uiState: LeaderInventoryUiState,
    onBack: () -> Unit,
    onAddProduct: () -> Unit
) {
    LeaderScaffold(
        title = "Cooperative Inventory",
        subtitle = "Detailed stock tracking and fair-price monitoring.",
        showBack = true,
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = AccentForest,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CardBackground)
        ) {
            when (uiState) {
                is LeaderInventoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentForest, strokeWidth = 2.dp)
                    }
                }
                is LeaderInventoryUiState.Success -> {
                    if (uiState.products.isEmpty()) {
                        EmptyInventoryState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp)
                        ) {
                            items(uiState.products, key = { it.productId }) { product ->
                                InventoryManagementCard(product)
                            }
                        }
                    }
                }
                is LeaderInventoryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = WarningRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryManagementCard(product: Product) {
    var isExpanded by remember { mutableStateOf(false) }
    val isBelowMsp = product.pricePerUnit < product.mspPerUnit
    val isLowStock = product.availableStock < 10

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Image, Title, Price & Expand icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentWarm.copy(alpha = 0.2f))
                ) {
                    AsyncImage(
                        model = product.imageUrls.firstOrNull(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Product info block
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${product.pricePerUnit.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            color = AccentForest,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " / ${product.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                        )
                    }
                }

                // Right column: stock badge & expand icon
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Stock indicator with dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .background(
                                if (isLowStock) LowStockBg else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isLowStock) WarningRed else AccentForest)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${product.availableStock} in stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLowStock) WarningRed else AccentForest,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AccentWarm,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expanded section
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Two-column stat grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatColumn(label = "MARKET PRICE (MSP)", value = "₹${product.mspPerUnit.toInt()}")
                    StatColumn(label = "BATCH LIMIT", value = "${product.preorderLimit} units")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Product description
                Text(
                    text = "PRODUCT SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark.copy(alpha = 0.8f),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // MSP warning
                if (isBelowMsp) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = WarningRed.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Fair‑trade violation: Listed price is below MSP.",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

@Composable
private fun EmptyInventoryState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = AccentWarm.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your warehouse is empty.",
                style = MaterialTheme.typography.titleMedium,
                color = TextMuted
            )
            Text(
                text = "Start by listing your first forest product.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted.copy(alpha = 0.7f)
            )
        }
    }
}
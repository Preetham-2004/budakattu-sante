package com.budakattu.sante.feature.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.budakattu.sante.core.ui.components.LeaderScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.*
import com.budakattu.sante.domain.model.OrderStatus

@Composable
fun BuyerOrdersRoute(
    activeRoute: String,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
    onNavigate: (String) -> Unit,
    onOpenCart: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: BuyerOrdersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HeritageScaffold(
        title = "Order Drumbeat",
        subtitle = "Review your current reservations, confirmed orders, and dispatch promises.",
        showBack = true,
        onBack = onBack,
    ) { padding ->
        when (val state = uiState) {
            BuyerOrdersUiState.Loading -> LoadingBody(padding)
            BuyerOrdersUiState.Unauthenticated -> MessageBody(padding, "Please sign in to view your orders.")
            is BuyerOrdersUiState.Error -> MessageBody(padding, state.message)
            is BuyerOrdersUiState.Content -> BuyerOrdersScreen(
                padding = padding,
                activeRoute = activeRoute,
                marketRoute = marketRoute,
                cartRoute = cartRoute,
                profileRoute = profileRoute,
                onNavigate = onNavigate,
                onOpenCart = onOpenCart,
                onOpenOrder = onOpenOrder,
                state = state,
            )
        }
    }
}

@Composable
fun CartRoute(
    activeRoute: String,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onOpenConfirmation: (String) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.NavigateToConfirmation -> onOpenConfirmation(event.orderId)
                is CartEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    HeritageScaffold(
        title = "Cart And Checkout",
        subtitle = "Assemble ready orders and pre-book requests here.",
        showBack = false,
    ) { padding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            when (val state = uiState) {
                CartUiState.Loading -> LoadingBody(padding, innerPadding)
                CartUiState.Unauthenticated -> MessageBody(padding, "Please sign in to manage your cart.", innerPadding)
                is CartUiState.Error -> MessageBody(padding, state.message, innerPadding)
                CartUiState.Empty -> EmptyCartBody(padding, innerPadding, onBack)
                is CartUiState.Content -> CartScreen(
                    state = state.cart,
                    isProcessing = state.isProcessing,
                    activeRoute = activeRoute,
                    marketRoute = marketRoute,
                    cartRoute = cartRoute,
                    profileRoute = profileRoute,
                    onNavigate = onNavigate,
                    outerPadding = padding,
                    innerPadding = innerPadding,
                    onBack = onBack,
                    onDecrease = viewModel::decreaseQuantity,
                    onIncrease = viewModel::increaseQuantity,
                    onRemove = viewModel::removeItem,
                    onCheckout = viewModel::checkout,
                )
            }
        }
    }
}

@Composable
fun OrderConfirmationRoute(
    orderId: String,
    onViewOrders: () -> Unit,
    onBackToMarket: () -> Unit,
    onBack: () -> Unit,
) {
    HeritageScaffold(
        title = "Preorder Confirmed",
        subtitle = "Your request has been passed to the cooperative.",
        showBack = true,
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ForestCard {
                Text("Order reference", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(orderId, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Secure Stripe payment captured. Your preorder has been successfully transmitted to the tribal cooperative for processing.", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Button(
                modifier = Modifier.fillMaxWidth(), 
                onClick = onViewOrders,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            ) {
                Text("View order history")
            }
            Button(
                modifier = Modifier.fillMaxWidth(), 
                onClick = onBackToMarket,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
            ) {
                Text("Back to market")
            }
        }
    }
}

@Composable
fun OrderDetailRoute(
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HeritageScaffold(
        title = "Order Tracking",
        subtitle = "See where your preorder sits in the cooperative flow.",
        showBack = true,
        onBack = onBack,
    ) { padding ->
        when (val state = uiState) {
            OrderDetailUiState.Loading -> LoadingBody(padding)
            is OrderDetailUiState.Error -> MessageBody(padding, state.message)
            is OrderDetailUiState.Content -> OrderDetailScreen(padding, state.order, onBack)
        }
    }
}

@Composable
fun LeaderOrdersRoute(
    onBack: () -> Unit,
    viewModel: LeaderOrdersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LeaderScaffold(
        title = "Order Fulfillment",
        subtitle = "Monitor reservations, confirm dispatch readiness, and close the loop.",
        showBack = true,
        onBack = onBack,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                LeaderOrdersUiState.Loading -> LoadingBody(PaddingValues(0.dp))
                is LeaderOrdersUiState.Error -> MessageBody(PaddingValues(0.dp), state.message)
                is LeaderOrdersUiState.Content -> LeaderOrdersContent(
                    state = state,
                    onUpdateStatus = viewModel::updateStatus,
                )
            }
        }
    }
}

@Composable
private fun BuyerOrdersScreen(
    padding: PaddingValues,
    activeRoute: String,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
    onNavigate: (String) -> Unit,
    onOpenCart: () -> Unit,
    onOpenOrder: (String) -> Unit,
    state: BuyerOrdersUiState.Content,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            BuyerRouteStrip(
                activeRoute = activeRoute,
                onNavigate = onNavigate,
                marketRoute = marketRoute,
                cartRoute = cartRoute,
                profileRoute = profileRoute,
            )
        }
        item {
            ForestCard {
                Text("Buyer order trail", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("Review reservations, confirmations, and dispatch promises before the next seasonal cycle moves.", color = MaterialTheme.colorScheme.onSurface)
                Button(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp), 
                    onClick = onOpenCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Open cart", fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.orders.isEmpty()) {
            item { EmptyStateCard("No orders yet", "Your order history will appear here once you place a ready order or a pre-book request.") }
        } else {
            items(state.orders, key = { it.orderId }) { order ->
                ForestCard {
                    Text(order.orderId, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(order.summary, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RouteBadge(label = "Status", value = order.statusLabel)
                        RouteBadge(label = "Type", value = order.typeLabel)
                    }
                    Text(order.totalAmountLabel, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 10.dp))
                    order.dispatchLabel?.let {
                        Text(it, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp), 
                        onClick = { onOpenOrder(order.orderId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    ) {
                        Text("Track order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartScreen(
    state: CartUi,
    isProcessing: Boolean,
    activeRoute: String,
    marketRoute: String,
    cartRoute: String,
    profileRoute: String,
    onNavigate: (String) -> Unit,
    outerPadding: PaddingValues,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onDecrease: (String, Int) -> Unit,
    onIncrease: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                BuyerRouteStrip(
                    activeRoute = activeRoute,
                    onNavigate = onNavigate,
                    marketRoute = marketRoute,
                    cartRoute = cartRoute,
                    profileRoute = profileRoute,
                )
            }

            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }

            items(state.items, key = { it.itemId }) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.sourceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = item.priceLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("MODE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 8.sp)
                                    Text(item.availabilityLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("QTY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 8.sp)
                                    Text(item.quantity.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onDecrease(item.itemId, item.quantity) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onIncrease(item.itemId, item.quantity) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onRemove(item.itemId) },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Remove", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Checkout summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${state.totalItems} item(s)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = state.totalAmountLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Secure payment powered by Stripe. Cooperative confirmation happens after this step.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(28.dp),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Confirm checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailScreen(
    padding: PaddingValues,
    order: OrderDetailUi,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
                Text("Back")
            }
        }
        item {
            ForestCard {
                Text(order.orderId, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RouteBadge(label = "Status", value = order.statusLabel)
                    RouteBadge(label = "Type", value = order.typeLabel)
                }
                Text(order.totalAmountLabel, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 10.dp))
                order.dispatchLabel?.let { Text(it, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.primary) }
            }
        }
        items(order.items, key = { it.productName + it.quantityLabel }) { item ->
            ForestCard {
                Text(item.productName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(item.quantityLabel, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurface)
                Text(item.sourceLabel, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                item.dispatchLabel?.let { Text(it, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

@Composable
private fun LeaderOrdersContent(
    state: LeaderOrdersUiState.Content,
    onUpdateStatus: (String, OrderStatus) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.orders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No pending orders found.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(state.orders, key = { it.orderId }) { order ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderId, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = order.statusLabel.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(order.summary, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Buyer: ${order.buyerLabel}", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        
                        if (order.dispatchLabel != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(order.dispatchLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (order.status == OrderStatus.RESERVED) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onUpdateStatus(order.orderId, OrderStatus.CONFIRMED) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LeaderPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("CONFIRM", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { onUpdateStatus(order.orderId, OrderStatus.COMPLETED) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LeaderSecondary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("COMPLETE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingBody(outerPadding: PaddingValues, innerPadding: PaddingValues = PaddingValues()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageBody(
    outerPadding: PaddingValues,
    message: String,
    innerPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyStateCard("Notice", message)
    }
}

@Composable
private fun EmptyCartBody(
    outerPadding: PaddingValues,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        EmptyStateCard("Cart is empty", "Add a ready product or a pre-book product from the catalog to start checkout.")
        Button(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
            Text("Back to market")
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    body: String,
) {
    ForestCard {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(body, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

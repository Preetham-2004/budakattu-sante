package com.budakattu.sante.feature.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    heritageRoute: String,
    ordersRoute: String,
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
        onBack = onBack
    ) { padding ->
        when (val state = uiState) {
            BuyerOrdersUiState.Loading -> LoadingBody(padding)
            BuyerOrdersUiState.Unauthenticated -> MessageBody(padding, "Please sign in to view your orders.")
            is BuyerOrdersUiState.Error -> MessageBody(padding, state.message)
            is BuyerOrdersUiState.Content -> BuyerOrdersScreen(
                padding = padding,
                activeRoute = activeRoute,
                marketRoute = marketRoute,
                heritageRoute = heritageRoute,
                ordersRoute = ordersRoute,
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
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ForestCard {
                Text("Order reference", style = MaterialTheme.typography.titleLarge)
                Text(orderId, modifier = Modifier.padding(top = 8.dp))
                Text("Mock payment captured. Cooperative confirmation will update the status automatically.", modifier = Modifier.padding(top = 8.dp))
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onViewOrders) {
                Text("View order history")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onBackToMarket) {
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
        onBack = onBack
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
        onBack = onBack
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
    heritageRoute: String,
    ordersRoute: String,
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
                heritageRoute = heritageRoute,
                ordersRoute = ordersRoute,
                profileRoute = profileRoute,
            )
        }
        item {
            ForestCard {
                Text("Buyer order trail", style = MaterialTheme.typography.titleLarge)
                Text("Review reservations, confirmations, and dispatch promises before the next seasonal cycle moves.")
                Button(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp), 
                    onClick = onOpenCart,
                    colors = ButtonDefaults.buttonColors(contentColor = Color.White)
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
                    Text(order.orderId, style = MaterialTheme.typography.titleLarge)
                    Text(order.summary, modifier = Modifier.padding(top = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RouteBadge(label = "Status", value = order.statusLabel)
                        RouteBadge(label = "Type", value = order.typeLabel)
                    }
                    Text(order.totalAmountLabel, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 10.dp))
                    order.dispatchLabel?.let {
                        Text(it, modifier = Modifier.padding(top = 4.dp))
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp), 
                        onClick = { onOpenOrder(order.orderId) },
                        colors = ButtonDefaults.buttonColors(contentColor = Color.White)
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
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalInk
                        )
                        Text(
                            text = item.sourceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = item.priceLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                                color = Color.White
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("MODE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                                    Text(item.availabilityLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                                color = Color.White
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("QTY", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                                    Text(item.quantity.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Checkout summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${state.totalItems} item(s)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = state.totalAmountLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = CharcoalInk
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Payment is mock-only. Cooperative confirmation happens after this step.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Confirm checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                Text(order.orderId, style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RouteBadge(label = "Status", value = order.statusLabel)
                    RouteBadge(label = "Type", value = order.typeLabel)
                }
                Text(order.totalAmountLabel, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 10.dp))
                order.dispatchLabel?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
            }
        }
        items(order.items, key = { it.productName + it.quantityLabel }) { item ->
            ForestCard {
                Text(item.productName, style = MaterialTheme.typography.titleLarge)
                Text(item.quantityLabel, modifier = Modifier.padding(top = 6.dp))
                Text(item.sourceLabel, modifier = Modifier.padding(top = 4.dp))
                item.dispatchLabel?.let { Text(it, modifier = Modifier.padding(top = 4.dp)) }
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
                    color = LeaderSurface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, LeaderSecondary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderId, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = LeaderPrimary)
                            Surface(
                                color = LeaderHighlight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = order.statusLabel.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LeaderSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(order.summary, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Buyer: ${order.buyerLabel}", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        
                        if (order.dispatchLabel != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = LeaderAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(order.dispatchLabel, style = MaterialTheme.typography.labelSmall, color = LeaderAccent, fontWeight = FontWeight.Bold)
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
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, modifier = Modifier.padding(top = 8.dp))
    }
}

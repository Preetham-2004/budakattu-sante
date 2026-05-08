package com.budakattu.sante.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budakattu.sante.core.ui.components.BuyerRouteStrip
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
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
    viewModel: BuyerOrdersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HeritageScaffold(
        title = "Order Drumbeat",
        subtitle = "Review your current reservations, confirmed orders, and dispatch promises from one buyer command surface.",
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
        subtitle = "Assemble ready orders and pre-book requests here before sending them to the cooperative.",
    ) { padding ->
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
) {
    HeritageScaffold(
        title = "Preorder Confirmed",
        subtitle = "Your request has been passed to the cooperative. Tracking and fulfilment now continue from the order trail.",
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
        subtitle = "See where your preorder sits in the cooperative flow, from reservation through dispatch.",
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
    HeritageScaffold(
        title = "Pending Cooperative Orders",
        subtitle = "Monitor reservations, confirm dispatch readiness, and close the loop on cooperative fulfilment.",
    ) { padding ->
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            when (val state = uiState) {
                LeaderOrdersUiState.Loading -> LoadingBody(padding, innerPadding)
                is LeaderOrdersUiState.Error -> MessageBody(padding, state.message, innerPadding)
                is LeaderOrdersUiState.Content -> LeaderOrdersScreen(
                    padding = padding,
                    innerPadding = innerPadding,
                    state = state,
                    onBack = onBack,
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
                Button(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), onClick = onOpenCart) {
                    Text("Open cart")
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
                    Button(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), onClick = { onOpenOrder(order.orderId) }) {
                        Text("Track order")
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
                Text("Back")
            }
        }
        items(state.items, key = { it.itemId }) { item ->
            ForestCard {
                Text(item.productName, style = MaterialTheme.typography.titleLarge)
                Text(item.sourceLabel, modifier = Modifier.padding(top = 6.dp))
                Text(item.priceLabel, modifier = Modifier.padding(top = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RouteBadge(label = "Mode", value = item.availabilityLabel)
                    RouteBadge(label = "Qty", value = item.quantity.toString())
                }
                item.dispatchLabel?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(modifier = Modifier.weight(1f), onClick = { onDecrease(item.itemId, item.quantity) }) { Text("-") }
                    Button(modifier = Modifier.weight(1f), onClick = { onIncrease(item.itemId, item.quantity) }) { Text("+") }
                    Button(modifier = Modifier.weight(1f), onClick = { onRemove(item.itemId) }) { Text("Remove") }
                }
            }
        }
        item {
            ForestCard {
                Text("Checkout summary", style = MaterialTheme.typography.titleLarge)
                Text("${state.totalItems} item(s)")
                Text(state.totalAmountLabel, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 6.dp))
                Text("Payment is mock-only. Cooperative confirmation happens after this step.", modifier = Modifier.padding(top = 8.dp))
                Button(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), onClick = onCheckout) {
                    Text("Confirm checkout")
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
private fun LeaderOrdersScreen(
    padding: PaddingValues,
    innerPadding: PaddingValues,
    state: LeaderOrdersUiState.Content,
    onBack: () -> Unit,
    onUpdateStatus: (String, OrderStatus) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
                Text("Back")
            }
        }
        if (state.orders.isEmpty()) {
            item { EmptyStateCard("No pending orders", "New preorder and checkout requests will appear here for leader action.") }
        } else {
            items(state.orders, key = { it.orderId }) { order ->
                ForestCard {
                    Text(order.orderId, style = MaterialTheme.typography.titleLarge)
                    Text(order.summary, modifier = Modifier.padding(top = 6.dp))
                    Text("Buyer: ${order.buyerLabel}", modifier = Modifier.padding(top = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RouteBadge(label = "Status", value = order.statusLabel)
                        RouteBadge(label = "Type", value = order.typeLabel)
                    }
                    order.dispatchLabel?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (order.status == OrderStatus.RESERVED) {
                            Button(modifier = Modifier.weight(1f), onClick = { onUpdateStatus(order.orderId, OrderStatus.CONFIRMED) }) {
                                Text("Confirm")
                            }
                        }
                        Button(modifier = Modifier.weight(1f), onClick = { onUpdateStatus(order.orderId, OrderStatus.COMPLETED) }) {
                            Text("Complete")
                        }
                        Button(modifier = Modifier.weight(1f), onClick = { onUpdateStatus(order.orderId, OrderStatus.CANCELLED) }) {
                            Text("Cancel")
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

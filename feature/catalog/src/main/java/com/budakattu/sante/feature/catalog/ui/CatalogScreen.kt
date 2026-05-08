package com.budakattu.sante.feature.catalog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.BuyerRouteStrip
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.MspBadge
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.feature.catalog.viewmodel.ProductListViewModel

@Composable
fun CatalogRoute(
    onOpenProduct: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.NavigateToDetail -> onOpenProduct(event.productId)
            }
        }
    }

    CatalogScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onProductClick = viewModel::onProductClick,
        onOpenRoute = onOpenRoute,
    )
}

@Composable
fun CatalogScreen(
    uiState: ProductListUiState,
    snackbarHostState: SnackbarHostState,
    onProductClick: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
) {
    HeritageScaffold(
        title = "Forest Market Command",
        subtitle = "Seasonal tribal produce, fair pricing, and traceable stories presented with stronger marketplace presence.",
    ) { innerPadding ->
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ) { contentPadding ->
            when (uiState) {
                ProductListUiState.Loading -> LoadingState(innerPadding, contentPadding)
                ProductListUiState.Offline -> OfflineState(innerPadding, contentPadding)
                is ProductListUiState.Error -> ErrorState(innerPadding, contentPadding, uiState.message)
                is ProductListUiState.Success -> ProductList(
                    products = uiState.products,
                    isOffline = uiState.isOffline,
                    outerPadding = innerPadding,
                    innerPadding = contentPadding,
                    onProductClick = onProductClick,
                    onOpenRoute = onOpenRoute,
                )
            }
        }
    }
}

@Composable
private fun LoadingState(outerPadding: PaddingValues, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun OfflineState(outerPadding: PaddingValues, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text("Offline and no cached catalog yet.")
    }
}

@Composable
private fun ErrorState(outerPadding: PaddingValues, innerPadding: PaddingValues, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(message)
    }
}

@Composable
private fun ProductList(
    products: List<ProductUiModel>,
    isOffline: Boolean,
    outerPadding: PaddingValues,
    innerPadding: PaddingValues,
    onProductClick: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
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
            BuyerRouteStrip(
                activeRoute = "catalog",
                onNavigate = onOpenRoute,
                marketRoute = "catalog",
                heritageRoute = "heritage",
                ordersRoute = "orders",
                profileRoute = "profile",
            )
        }

        item {
            ForestCard {
                Text("Trading circle", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isOffline) {
                        "Showing the locally available catalog while the forest route is offline."
                    } else {
                        "Move between market, heritage, orders, and profile from one commanding surface."
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    RouteBadge(label = "Products", value = products.size.toString())
                    RouteBadge(label = "Mode", value = if (isOffline) "Offline" else "Live")
                }
            }
        }

        items(items = products, key = { it.id }) { product ->
            ForestCard(
                modifier = Modifier.clickable { onProductClick(product.id) },
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = product.categoryName, style = MaterialTheme.typography.labelLarge)
                    }
                    MspBadge(isSafe = product.isMspSafe)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = product.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    RouteBadge(label = "Mode", value = product.availabilityLabel)
                    RouteBadge(label = "Action", value = product.ctaLabel)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = product.priceLabel, style = MaterialTheme.typography.titleLarge)
                Text(text = product.stockLabel, style = MaterialTheme.typography.bodyLarge)
                product.expectedDispatchLabel?.let { dispatch ->
                    Text(text = dispatch, style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    text = "Harvested by ${product.familyName}, ${product.village}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                product.seasonLabel?.let { season ->
                    Text(text = "Season: $season", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

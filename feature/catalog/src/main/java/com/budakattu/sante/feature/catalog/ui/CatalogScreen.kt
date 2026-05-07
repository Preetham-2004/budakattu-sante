package com.budakattu.sante.feature.catalog.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.MspBadge
import com.budakattu.sante.core.ui.theme.ForestPrimary
import com.budakattu.sante.core.ui.theme.Parchment
import com.budakattu.sante.feature.catalog.viewmodel.ProductListViewModel

@Composable
fun CatalogRoute(
    onOpenProduct: (String) -> Unit,
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
    )
}

@Composable
fun CatalogScreen(
    uiState: ProductListUiState,
    snackbarHostState: SnackbarHostState,
    onProductClick: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Parchment,
    ) { innerPadding ->
        when (uiState) {
            ProductListUiState.Loading -> LoadingState(innerPadding)
            ProductListUiState.Offline -> OfflineState(innerPadding)
            is ProductListUiState.Error -> ErrorState(innerPadding, uiState.message)
            is ProductListUiState.Success -> ProductList(
                products = uiState.products,
                isOffline = uiState.isOffline,
                innerPadding = innerPadding,
                onProductClick = onProductClick,
            )
        }
    }
}

@Composable
private fun LoadingState(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ForestPrimary)
    }
}

@Composable
private fun OfflineState(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text("Offline and no cached catalog yet.")
    }
}

@Composable
private fun ErrorState(innerPadding: PaddingValues, message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
    innerPadding: PaddingValues,
    onProductClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Parchment)
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Budakattu-Sante",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ForestPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isOffline) {
                        "Showing cached catalog while the device is offline."
                    } else {
                        "Forest products sourced through tribal cooperatives."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        items(items = products, key = { it.id }) { product ->
            ForestCard(
                modifier = Modifier.clickable { onProductClick(product.id) },
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1471943311424-646960669fbc?w=900",
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
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
                Text(text = product.priceLabel, style = MaterialTheme.typography.titleLarge)
                Text(text = product.stockLabel, style = MaterialTheme.typography.bodyLarge)
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

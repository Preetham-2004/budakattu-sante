package com.budakattu.sante.feature.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.MspBadge
import com.budakattu.sante.core.ui.theme.Parchment

@Composable
fun ProductDetailRoute(
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    ProductDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAudioDescriptionClick = viewModel::onAudioDescriptionClick,
        onPreorderClick = viewModel::onPreorderClick,
    )
}

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    snackbarHostState: SnackbarHostState,
    onAudioDescriptionClick: () -> Unit,
    onPreorderClick: () -> Unit,
) {
    Scaffold(
        containerColor = Parchment,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            ProductDetailUiState.Loading -> Loading(innerPadding)
            is ProductDetailUiState.Error -> CenterText(innerPadding, uiState.message)
            is ProductDetailUiState.Success -> DetailContent(
                state = uiState,
                innerPadding = innerPadding,
                onAudioDescriptionClick = onAudioDescriptionClick,
                onPreorderClick = onPreorderClick,
            )
        }
    }
}

@Composable
private fun Loading(innerPadding: PaddingValues) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenterText(innerPadding: PaddingValues, text: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text)
    }
}

@Composable
private fun DetailContent(
    state: ProductDetailUiState.Success,
    innerPadding: PaddingValues,
    onAudioDescriptionClick: () -> Unit,
    onPreorderClick: () -> Unit,
) {
    val product = state.product
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AsyncImage(
                model = product.imageUrls.firstOrNull(),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop,
            )
        }
        item {
            ForestCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.headlineMedium)
                        Text(product.categoryName, style = MaterialTheme.typography.labelLarge)
                    }
                    MspBadge(isSafe = product.isMspSafe)
                }
                Text(product.description, modifier = Modifier.padding(top = 12.dp))
                Text(product.priceLabel, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
                Text(product.mspLabel)
                Text(product.stockLabel)
                if (state.isOffline) {
                    Text("Viewing cached product details offline.", modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        item {
            ForestCard {
                Text("Traceability", style = MaterialTheme.typography.titleLarge)
                Text(product.traceabilityLabel, modifier = Modifier.padding(top = 8.dp))
                Text("Harvested by ${product.familyTitle}", modifier = Modifier.padding(top = 8.dp))
                Text("Village: ${product.village}")
                Text("Season: ${product.harvestWindow}")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(modifier = Modifier.weight(1f), onClick = onAudioDescriptionClick) {
                    Text("Hear audio")
                }
                Button(modifier = Modifier.weight(1f), onClick = onPreorderClick) {
                    Text("Preorder")
                }
            }
        }
        item {
            Text("Related products", style = MaterialTheme.typography.titleLarge)
        }
        items(state.relatedProducts, key = { it.id }) { related ->
            ForestCard {
                Text(related.title, style = MaterialTheme.typography.titleLarge)
                Text(related.subtitle)
            }
        }
    }
}

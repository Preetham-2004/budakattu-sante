package com.budakattu.sante.feature.catalog.ui

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.*
import com.budakattu.sante.core.ui.theme.ForestPrimary
import com.budakattu.sante.feature.catalog.viewmodel.ProductListViewModel
import com.budakattu.sante.feature.catalog.viewmodel.SupportChatViewModel

@Composable
fun CatalogRoute(
    onOpenProduct: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    onBack: () -> Unit,
    paymentSuccess: Boolean = false,
    onClearPaymentSuccess: () -> Unit = {},
    viewModel: ProductListViewModel = hiltViewModel(),
    chatViewModel: SupportChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chatMessages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showChat by remember { mutableStateOf(value = false) }

    LaunchedEffect(paymentSuccess) {
        if (paymentSuccess) {
            snackbarHostState.showSnackbar("Payment Successful! Your order has been placed.")
            onClearPaymentSuccess()
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { spokenText ->
                chatViewModel.sendMessage(spokenText)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.NavigateToDetail -> onOpenProduct(event.productId)
            }
        }
    }

    Box {
        CatalogScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onProductClick = viewModel::onProductClick,
            onOpenRoute = onOpenRoute,
            onBack = onBack,
        ) { showChat = true }

        if (showChat) {
            SupportChatBottomSheet(
                onDismissRequest = { showChat = false },
                onSendMessage = chatViewModel::sendMessage,
                onVoiceInputClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        // This allows for any regional language supported by Google
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                    }
                    speechLauncher.launch(intent)
                },
                messages = chatMessages,
                isTyping = isTyping,
            )
        }
    }
}

@Composable
fun CatalogScreen(
    uiState: ProductListUiState,
    snackbarHostState: SnackbarHostState,
    onProductClick: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
) {
    val successState = uiState as? ProductListUiState.Success
    HeritageScaffold(
        title = "Marketplace",
        subtitle = "Authentic tribal harvest directly from the forest.",
        showBack = true,
        onBack = onBack,
        topBarContent = {
            if ((successState != null) && (successState.userName != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = successState.userProfilePictureUrl ?: "https://i.pravatar.cc/150?u=${successState.userName}",
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Namaste, ${successState.userName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            floatingActionButton = {
                SupportChatFAB(onClick = onOpenChat)
            }
        ) { contentPadding ->
            when (uiState) {
                ProductListUiState.Loading -> LoadingState(innerPadding, contentPadding)
                ProductListUiState.Offline -> OfflineState(innerPadding, contentPadding)
                is ProductListUiState.Error -> ErrorState(innerPadding, contentPadding, uiState.message)
                is ProductListUiState.Success -> ProductGrid(
                    products = uiState.products,
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
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ForestPrimary)
    }
}

@Composable
private fun OfflineState(outerPadding: PaddingValues, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text("Offline. Please check your connection.")
    }
}

@Composable
private fun ErrorState(outerPadding: PaddingValues, innerPadding: PaddingValues, message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(message)
    }
}

@Composable
private fun ProductGrid(
    products: List<ProductUiModel>,
    outerPadding: PaddingValues,
    innerPadding: PaddingValues,
    onProductClick: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            BuyerRouteStrip(
                activeRoute = "catalog",
                onNavigate = onOpenRoute,
                marketRoute = "catalog",
                cartRoute = "cart",
                profileRoute = "profile",
            )
        }

        item(span = { GridItemSpan(2) }) {
            SearchBar()
        }

        items(items = products, key = { it.id }) { product ->
            ProductGridItem(product = product) {
                onProductClick(product.id)
            }
        }
    }
}

@Composable
private fun SearchBar() {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Search forest products...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ProductGridItem(
    product: ProductUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop,
                )
                if (product.ctaLabel == "Pre-book") {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "PRE-BOOK",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    MspBadge(isSafe = product.isMspSafe)
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = product.categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = product.priceLabel.substringBefore("/"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "/" + product.priceLabel.substringAfter("/"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

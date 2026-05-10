package com.budakattu.sante.feature.productdetail

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.HighlightCard
import com.budakattu.sante.core.ui.components.MspBadge
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.*
import java.util.Locale

@Composable
fun ProductDetailRoute(
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val textToSpeech = remember(context) {
        TextToSpeech(context, null)
    }

    DisposableEffect(textToSpeech) {
        textToSpeech.language = Locale.ENGLISH
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    ProductDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAudioDescriptionClick = {
            val state = uiState as? ProductDetailUiState.Success
            if (state != null) {
                textToSpeech.speak(
                    state.product.audioDescription,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    state.product.id,
                )
                viewModel.onAudioDescriptionClick()
            }
        },
        onPaymentClick = viewModel::onPaymentClick
    )
}

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onAudioDescriptionClick: () -> Unit,
    onPaymentClick: (Int) -> Unit,
) {
    HeritageScaffold(
        title = "Product Detail",
        subtitle = "Forest harvest with traceability and fair-price commitment.",
        showBack = true,
        onBack = onBack
    ) { outerPadding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            when (uiState) {
                ProductDetailUiState.Loading -> Loading(outerPadding, innerPadding)
                is ProductDetailUiState.Error -> CenterText(outerPadding, innerPadding, uiState.message)
                is ProductDetailUiState.Success -> DetailContent(
                    state = uiState,
                    outerPadding = outerPadding,
                    innerPadding = innerPadding,
                    onAudioDescriptionClick = onAudioDescriptionClick,
                    onPaymentClick = onPaymentClick,
                )
            }
        }
    }
}

@Composable
private fun Loading(outerPadding: PaddingValues, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ForestPrimary)
    }
}

@Composable
private fun CenterText(outerPadding: PaddingValues, innerPadding: PaddingValues, text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text)
    }
}

@Composable
private fun DetailContent(
    state: ProductDetailUiState.Success,
    outerPadding: PaddingValues,
    innerPadding: PaddingValues,
    onAudioDescriptionClick: () -> Unit,
    onPaymentClick: (Int) -> Unit,
) {
    val product = state.product
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box {
                    AsyncImage(
                        model = product.imageUrls.firstOrNull(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(
                        onClick = onAudioDescriptionClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ForestPrimary)
                    }
                }
            }
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = ForestPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.priceLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = SunsetClay
                    )
                    MspBadge(isSafe = product.isMspSafe)
                }
            }
        }

        item {
            HighlightCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = ForestPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Product Owner & Admin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = ForestBackground,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = product.familyTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = TraditionalPrimary
                        )
                        Text(
                            text = "Official Cooperative Administrator",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        
                        product.familyDetails?.let { details ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Expertise: ${details.primaryCraft}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Region: ${details.forestRegion}, ${details.district}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalInk.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Village: ${product.village}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Origin Story", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                
                val displayStory = product.familyDetails?.story ?: product.description
                Text(text = displayStory, style = MaterialTheme.typography.bodyMedium, color = CharcoalInk.copy(alpha = 0.8f))
            }
        }

        item {
            ForestCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity -= 1 },
                            modifier = Modifier.size(36.dp).background(ForestBackground, CircleShape)
                        ) { Icon(Icons.Default.Remove, contentDescription = null) }
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(
                            onClick = { quantity += 1 },
                            modifier = Modifier.size(36.dp).background(ForestBackground, CircleShape)
                        ) { Icon(Icons.Default.Add, contentDescription = null) }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { onPaymentClick(quantity) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = ForestPrimary.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    enabled = !state.isProcessingPayment && product.ctaLabel != "Unavailable"
                ) {
                    if (state.isProcessingPayment) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("PROCESSING...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(product.ctaLabel.uppercase(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (product.ctaLabel != "Unavailable") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Secure payment powered by Razorpay",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

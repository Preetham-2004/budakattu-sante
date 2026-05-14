package com.budakattu.sante.feature.productdetail

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
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
import com.budakattu.sante.core.ui.components.*
import com.budakattu.sante.core.ui.theme.*
import java.util.Locale

@Composable
fun ProductDetailRoute(
    onBack: () -> Unit,
    onNavigateToMarket: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    chatViewModel: SupportChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chatMessages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showChat by remember { mutableStateOf(value = false) }
    var showPaymentDialog by remember { mutableStateOf(value = false) }
    var pendingQuantity by remember { mutableIntStateOf(1) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { text ->
                chatViewModel.sendMessage(text)
            }
        }
    }

    val context = LocalContext.current
    val textToSpeech = remember(context) {
        TextToSpeech(context, null)
    }

    DisposableEffect(textToSpeech) {
        textToSpeech.language = Locale.getDefault()
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ProductDetailEvent.OrderSuccess -> onNavigateToMarket()
            }
        }
    }

    Box {
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
            onPaymentClick = { quantity ->
                pendingQuantity = quantity
                showPaymentDialog = true
            },
            onAddToCartClick = viewModel::addToCart,
        ) { showChat = true }

        if (showPaymentDialog) {
            val product = (uiState as? ProductDetailUiState.Success)?.product
            DummyPaymentDialog(
                amount = "Rs ${(product?.pricePerUnit ?: 0f) * pendingQuantity}",
                onPaymentComplete = {
                    showPaymentDialog = false
                    viewModel.onPaymentClick(pendingQuantity)
                },
                onDismiss = { showPaymentDialog = false },
            )
        }

        if (showChat) {
            SupportChatBottomSheet(
                onDismissRequest = { showChat = false },
                onSendMessage = chatViewModel::sendMessage,
                onVoiceInputClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onAudioDescriptionClick: () -> Unit,
    onPaymentClick: (Int) -> Unit,
    onAddToCartClick: (Int) -> Unit,
    onOpenChat: () -> Unit,
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
            floatingActionButton = {
                SupportChatFAB(onClick = onOpenChat)
            }
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
                    onAddToCartClick = onAddToCartClick,
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
    onAddToCartClick: (Int) -> Unit,
) {
    val product = state.product
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    val totalPrice = product.pricePerUnit * quantity
    
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
                color = MaterialTheme.colorScheme.surface,
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
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    }
                }
            }
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rs ${totalPrice.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Total for $quantity ${product.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    MspBadge(isSafe = product.isMspSafe)
                }
            }
        }

        item {
            HighlightCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Product Owner & Admin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = product.familyTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Official Cooperative Administrator",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        product.familyDetails?.let { details ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Expertise: ${details.primaryCraft}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Region: ${details.forestRegion}, ${details.district}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Village: ${product.village}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Origin Story", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                
                val displayStory = product.familyDetails?.story ?: product.description
                Text(text = displayStory, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }

        item {
            ForestCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity -= 1 },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { quantity += 1 },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedButton(
                    onClick = { onAddToCartClick(quantity) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    enabled = product.ctaLabel != "Unavailable"
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("ADD TO CART", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { onPaymentClick(quantity) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    ),
                    enabled = (!state.isProcessingPayment && (product.ctaLabel != "Unavailable")),
                ) {
                    if (state.isProcessingPayment) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("PROCESSING...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SECURE PAYMENT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (product.ctaLabel != "Unavailable") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Secure payment powered by Razorpay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

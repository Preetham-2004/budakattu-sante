package com.budakattu.sante.feature.productdetail

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.budakattu.sante.core.ui.components.*
import com.budakattu.sante.core.ui.theme.*
import com.budakattu.sante.feature.productdetail.SupportChatViewModel
import java.util.Locale

@Composable
fun ProductDetailRoute(
    onBack: () -> Unit,
    onNavigateToMarket: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    chatViewModel: SupportChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chatMessages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showChat by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var pendingQuantity by remember { mutableIntStateOf(1) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                chatViewModel.sendMessage(spokenText)
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
            onOpenChat = { showChat = true }
        )

        if (showPaymentDialog) {
            val product = (uiState as? ProductDetailUiState.Success)?.product
            DummyPaymentDialog(
                amount = "Rs ${(product?.pricePerUnit ?: 0f) * pendingQuantity}",
                onPaymentComplete = {
                    showPaymentDialog = false
                    viewModel.onPaymentClick(pendingQuantity)
                },
                onDismiss = { showPaymentDialog = false }
            )
        }

        if (showChat) {
            SupportChatBottomSheet(
                onDismissRequest = { showChat = false },
                onSendMessage = chatViewModel::sendMessage,
                onVoiceInputClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
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
                    Column {
                        Text(
                            text = "Rs ${totalPrice.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SunsetClay,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Total for $quantity ${product.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
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
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MistVeil)
                                .border(1.dp, ForestPrimary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = ForestPrimary)
                        }
                        Text(
                            text = quantity.toString(),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalInk
                        )
                        IconButton(
                            onClick = { quantity += 1 },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MistVeil)
                                .border(1.dp, ForestPrimary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ForestPrimary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedButton(
                    onClick = { onAddToCartClick(quantity) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, ForestPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestPrimary),
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
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SECURE PAYMENT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

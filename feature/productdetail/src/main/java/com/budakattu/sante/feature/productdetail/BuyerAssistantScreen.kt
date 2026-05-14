package com.budakattu.sante.feature.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.domain.model.SeasonalRecommendation
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun BuyerAssistantRoute(
    onBack: () -> Unit,
    viewModel: BuyerAssistantViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf("") }

    HeritageScaffold(
        title = "Market Assistant",
        subtitle = "Ask about products, preorder timing, or harvest windows. Seasonal picks are shown below.",
    ) { padding ->
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
                    Text("Assistant chat", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Ask a question") },
                        minLines = 3,
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        onClick = {
                            viewModel.sendMessage(message)
                            message = ""
                        },
                    ) {
                        Text("Ask assistant")
                    }
                    Text(
                        "Gemini API key placeholder is configured in the data module build file. This screen falls back to local responses until a real key is wired.",
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            item {
                ForestCard {
                    Text("Assistant reply", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                    Text(uiState.reply, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                }
            }
            item {
                Text("Seasonal recommendations", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            }
            items(uiState.recommendations, key = { it.title }) { recommendation ->
                RecommendationCard(recommendation)
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: SeasonalRecommendation) {
    ForestCard {
        Text(recommendation.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(recommendation.summary, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurface)
        Text(recommendation.actionHint, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}

@HiltViewModel
class BuyerAssistantViewModel @Inject constructor(
    getProductsUseCase: GetProductsUseCase,
) : ViewModel() {
    private val _reply = MutableStateFlow("Ask about honey, harvest timing, usage tips, or preorder planning.")
    private val reply = _reply.asStateFlow()

    val uiState = getProductsUseCase()
        .map { products ->
            products
                .sortedWith(
                    compareByDescending<com.budakattu.sante.domain.model.Product> {
                        it.availability == ProductAvailability.PREBOOK_OPEN || it.availability == ProductAvailability.COMING_SOON
                    }.thenByDescending { it.availableStock },
                )
                .take(3)
                .map { product ->
                    SeasonalRecommendation(
                        title = product.name,
                        summary = "${product.categoryName} from ${product.familyName}, ${product.village}. ${product.season ?: "Year-round"} cycle.",
                        actionHint = when (product.availability) {
                            ProductAvailability.IN_STOCK -> "Order while this batch is ready."
                            ProductAvailability.PREBOOK_OPEN -> "Pre-book before the reservation window closes."
                            ProductAvailability.COMING_SOON -> "Watch the upcoming batch and book once it opens."
                            ProductAvailability.SOLD_OUT -> "Check again after the next harvest cycle."
                        },
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        .combine(reply) { recommendations, assistantReply ->
            BuyerAssistantUiState(
                reply = assistantReply,
                recommendations = recommendations,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BuyerAssistantUiState(
                reply = "Ask about honey, harvest timing, usage tips, or preorder planning.",
                recommendations = emptyList(),
            ),
        )

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _reply.value = localReply(message.trim().lowercase())
        }
    }

    private fun localReply(message: String): String {
        return when {
            "immunity" in message || "best honey" in message ->
                "Wild forest honey is usually the strongest fit for immunity-focused buyers. Pre-book early when the harvest window is narrow."
            "harvest" in message || "season" in message ->
                "Harvest timing follows bloom cycles, craft runs, and forest access windows. Seasonal products are safest to reserve before dispatch dates are announced."
            "use" in message || "how to" in message ->
                "Use the product detail page for audio guidance, pricing, and source information. Start with small household quantities if you are trying a new forest product."
            "prebook" in message || "pre-order" in message || "preorder" in message ->
                "Pre-booking reserves your place before the cooperative completes production or harvest. Reserved orders move to confirmed when the batch is ready."
            else ->
                "Budakattu-Sante helps you buy tribal cooperative products with preorder support and fair-price visibility. Ask about honey, harvest timing, usage tips, or preorder timing."
        }
    }
}

data class BuyerAssistantUiState(
    val reply: String,
    val recommendations: List<SeasonalRecommendation>,
)

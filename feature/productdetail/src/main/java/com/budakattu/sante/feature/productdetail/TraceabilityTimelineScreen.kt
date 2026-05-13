package com.budakattu.sante.feature.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.core.ui.components.ForestCard
import com.budakattu.sante.core.ui.components.HeritageScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.domain.model.BatchRecord
import com.budakattu.sante.domain.model.SupplyLog
import com.budakattu.sante.domain.model.TribalFamily
import com.budakattu.sante.domain.usecase.product.GetProductUseCase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@Composable
fun TraceabilityTimelineRoute(
    onBack: () -> Unit,
    viewModel: TraceabilityTimelineViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HeritageScaffold(
        title = "Product Source Trail",
        subtitle = "See the supplying family, batch record, and harvest movement behind this forest product.",
    ) { padding ->
        when (val state = uiState) {
            TraceabilityTimelineUiState.Loading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }

            is TraceabilityTimelineUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ForestCard {
                    Text("Traceability unavailable", style = MaterialTheme.typography.titleLarge)
                    Text(state.message, modifier = Modifier.padding(top = 8.dp))
                }
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onBack) { Text("Back") }
            }

            is TraceabilityTimelineUiState.Content -> TraceabilityTimelineScreen(
                padding = padding,
                state = state,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun TraceabilityTimelineScreen(
    padding: PaddingValues,
    state: TraceabilityTimelineUiState.Content,
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
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
                Text("Back")
            }
        }
        item {
            ForestCard {
                Text(state.productName, style = MaterialTheme.typography.titleLarge)
                Text("Supplier family: ${state.family?.familyName ?: "Unknown"}", modifier = Modifier.padding(top = 8.dp))
                Text("Village: ${state.family?.village ?: state.fallbackVillage}", modifier = Modifier.padding(top = 4.dp))
                Text("District: ${state.family?.district ?: "Not recorded"}", modifier = Modifier.padding(top = 4.dp))
                Text("Region: ${state.family?.forestRegion ?: "Forest belt"}", modifier = Modifier.padding(top = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RouteBadge(label = "Batches", value = state.batches.size.toString())
                    RouteBadge(label = "Logs", value = state.logs.size.toString())
                }
            }
        }
        item {
            ForestCard {
                Text("Family story", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = state.family?.story?.ifBlank {
                                "The cooperative has not added a story for this family yet."
                            } ?: "The cooperative has not added a story for this family yet.",
                            modifier = Modifier.padding(top = 8.dp),
                        )
            }
        }
        items(state.batches, key = { it.batchId }) { batch ->
            ForestCard {
                Text("Batch ${batch.batchId}", style = MaterialTheme.typography.titleLarge)
                Text("Harvest date: ${batch.harvestDate}", modifier = Modifier.padding(top = 6.dp))
                Text("Origin: ${batch.originVillage}", modifier = Modifier.padding(top = 4.dp))
                Text("${batch.quantity} ${batch.unit}", modifier = Modifier.padding(top = 4.dp))
                Text("Status: ${batch.status}", modifier = Modifier.padding(top = 4.dp))
            }
        }
        items(state.logs, key = { it.logId }) { log ->
            ForestCard {
                Text("Supply record", style = MaterialTheme.typography.titleLarge)
                Text("Family: ${log.familyName}", modifier = Modifier.padding(top = 6.dp))
                Text("Harvest date: ${log.harvestDate}", modifier = Modifier.padding(top = 4.dp))
                Text("Batch: ${log.batchId}", modifier = Modifier.padding(top = 4.dp))
                Text("${log.quantity} ${log.unit}", modifier = Modifier.padding(top = 4.dp))
                if (log.notes.isNotBlank()) {
                    Text(log.notes, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TraceabilityTimelineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getProductUseCase: GetProductUseCase,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val productId: String = savedStateHandle["productId"] ?: ""

    val uiState = getProductUseCase(productId)
        .flatMapLatest { product ->
            if (product == null) {
                flowOf(TraceabilityTimelineUiState.Error("Product not found"))
            } else {
                combine(
                    observeFamily(product.familyId),
                    observeSupplyLogsForProduct(product.productId),
                    observeBatchRecordsForProduct(product.productId),
                ) { family, logs, batches ->
                    TraceabilityTimelineUiState.Content(
                        productName = product.name,
                        family = family,
                        logs = logs,
                        batches = batches,
                        fallbackVillage = product.village,
                    ) as TraceabilityTimelineUiState
                }
            }
        }
        .catch { emit(TraceabilityTimelineUiState.Error(it.localizedMessage ?: "Unable to load traceability details")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TraceabilityTimelineUiState.Loading)

    private fun observeFamily(familyId: String) = callbackFlow<TribalFamily?> {
        if (familyId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("tribal_families").document(familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toFamily())
            }
        awaitClose { listener.remove() }
    }

    private fun observeSupplyLogsForProduct(productId: String) = callbackFlow<List<SupplyLog>> {
        val listener = firestore.collection("supply_logs")
            .whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toSupplyLog() }.orEmpty().sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    private fun observeBatchRecordsForProduct(productId: String) = callbackFlow<List<BatchRecord>> {
        val listener = firestore.collection("batch_records")
            .whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toBatchRecord() }.orEmpty().sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }
}

sealed interface TraceabilityTimelineUiState {
    data object Loading : TraceabilityTimelineUiState
    data class Error(val message: String) : TraceabilityTimelineUiState
    data class Content(
        val productName: String,
        val family: TribalFamily?,
        val logs: List<SupplyLog>,
        val batches: List<BatchRecord>,
        val fallbackVillage: String,
    ) : TraceabilityTimelineUiState
}

private fun com.google.firebase.firestore.DocumentSnapshot.toFamily(): TribalFamily? {
    val cooperativeId = getString("cooperativeId") ?: return null
    return TribalFamily(
        familyId = getString("familyId") ?: id,
        cooperativeId = cooperativeId,
        familyName = getString("familyName").orEmpty(),
        village = getString("village").orEmpty(),
        district = getString("district").orEmpty(),
        forestRegion = getString("forestRegion").orEmpty(),
        story = getString("story").orEmpty(),
        primaryCraft = getString("primaryCraft").orEmpty(),
        isActive = getBoolean("isActive") ?: true,
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L,
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toSupplyLog(): SupplyLog? {
    val cooperativeId = getString("cooperativeId") ?: return null
    return SupplyLog(
        logId = getString("logId") ?: id,
        cooperativeId = cooperativeId,
        productId = getString("productId").orEmpty(),
        productName = getString("productName").orEmpty(),
        familyId = getString("familyId").orEmpty(),
        familyName = getString("familyName").orEmpty(),
        batchId = getString("batchId").orEmpty(),
        quantity = (getLong("quantity") ?: 0L).toInt(),
        unit = getString("unit").orEmpty(),
        harvestDate = getString("harvestDate").orEmpty(),
        originVillage = getString("originVillage").orEmpty(),
        notes = getString("notes").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toBatchRecord(): BatchRecord? {
    val cooperativeId = getString("cooperativeId") ?: return null
    return BatchRecord(
        batchId = getString("batchId") ?: id,
        cooperativeId = cooperativeId,
        productId = getString("productId").orEmpty(),
        productName = getString("productName").orEmpty(),
        familyId = getString("familyId").orEmpty(),
        familyName = getString("familyName").orEmpty(),
        harvestDate = getString("harvestDate").orEmpty(),
        originVillage = getString("originVillage").orEmpty(),
        quantity = (getLong("quantity") ?: 0L).toInt(),
        unit = getString("unit").orEmpty(),
        status = getString("status").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L,
    )
}

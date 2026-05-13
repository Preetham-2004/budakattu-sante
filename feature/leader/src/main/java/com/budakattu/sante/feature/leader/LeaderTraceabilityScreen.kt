package com.budakattu.sante.feature.leader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budakattu.sante.core.ui.components.LeaderScaffold
import com.budakattu.sante.core.ui.components.RouteBadge
import com.budakattu.sante.core.ui.theme.*
import com.budakattu.sante.domain.model.Product
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.model.SupplyLog
import com.budakattu.sante.domain.model.TribalFamily
import com.budakattu.sante.domain.repository.ProductRepository
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun LeaderTraceabilityRoute(
    onBack: () -> Unit,
    viewModel: LeaderTraceabilityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LeaderScaffold(
        title = "Traceability Console",
        subtitle = "Manage family records and capture harvest supply logs.",
        showBack = true,
        onBack = onBack
    ) { outerPadding ->
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            when (val state = uiState) {
                LeaderTraceabilityUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LeaderPrimary)
                    }
                }

                is LeaderTraceabilityUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(outerPadding).padding(innerPadding).padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(state.message, color = LeaderError)
                    }
                }

                is LeaderTraceabilityUiState.Content -> LeaderTraceabilityContent(
                    outerPadding = outerPadding,
                    innerPadding = innerPadding,
                    state = state,
                    formState = formState,
                    onBack = onBack,
                    onFamilyNameChange = viewModel::updateFamilyName,
                    onVillageChange = viewModel::updateVillage,
                    onDistrictChange = viewModel::updateDistrict,
                    onRegionChange = viewModel::updateRegion,
                    onStoryChange = viewModel::updateStory,
                    onCraftChange = viewModel::updateCraft,
                    onSaveFamily = viewModel::saveFamily,
                    onSelectProduct = viewModel::selectProduct,
                    onSelectFamily = viewModel::selectFamily,
                    onQuantityChange = viewModel::updateQuantity,
                    onHarvestDateChange = viewModel::updateHarvestDate,
                    onNotesChange = viewModel::updateNotes,
                    onCreateSupplyLog = viewModel::createSupplyLog,
                )
            }
        }
    }
}

@Composable
private fun LeaderTraceabilityContent(
    outerPadding: PaddingValues,
    innerPadding: PaddingValues,
    state: LeaderTraceabilityUiState.Content,
    formState: LeaderTraceabilityFormState,
    onBack: () -> Unit,
    onFamilyNameChange: (String) -> Unit,
    onVillageChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onStoryChange: (String) -> Unit,
    onCraftChange: (String) -> Unit,
    onSaveFamily: () -> Unit,
    onSelectProduct: (String) -> Unit,
    onSelectFamily: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onHarvestDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCreateSupplyLog: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .padding(innerPadding),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            TraceabilityCard(title = "Add Tribal Family", icon = Icons.Default.Groups) {
                TraceField("Family Name", formState.familyName, onFamilyNameChange)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) { TraceField("Village", formState.village, onVillageChange) }
                    Box(modifier = Modifier.weight(1f)) { TraceField("District", formState.district, onDistrictChange) }
                }
                TraceField("Forest Region", formState.region, onRegionChange)
                TraceField("Primary Craft", formState.primaryCraft, onCraftChange)
                TraceField("Family Story", formState.story, onStoryChange, minLines = 3)
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSaveFamily,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeaderPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("REGISTER FAMILY", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            TraceabilityCard(title = "New Supply Log", icon = Icons.Default.HistoryEdu) {
                Text("Associated Product", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                SelectionRow(
                    items = state.products.map { it.productId to it.name },
                    selectedId = formState.selectedProductId,
                    onSelected = onSelectProduct,
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Supplying Family", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                SelectionRow(
                    items = state.families.map { it.familyId to it.familyName },
                    selectedId = formState.selectedFamilyId,
                    onSelected = onSelectFamily,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    Box(modifier = Modifier.weight(1f)) { TraceField("Qty", formState.quantity, onQuantityChange) }
                    Box(modifier = Modifier.weight(1f)) { TraceField("Harvest Date", formState.harvestDate, onHarvestDateChange) }
                }
                TraceField("Batch Notes", formState.notes, onNotesChange, minLines = 2)
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCreateSupplyLog,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeaderSecondary,
                        contentColor = Color.White
                    )
                ) {
                    Text("CREATE LOG", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text("LOG HISTORY", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = LeaderSecondary, letterSpacing = 1.sp)
        }

        items(state.logs, key = { it.logId }) { log ->
            LogItem(log)
        }
    }
}

@Composable
private fun TraceabilityCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LeaderSurface,
        border = BorderStroke(1.dp, LeaderSecondary.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = LeaderPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CharcoalInk)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun TraceField(label: String, value: String, onValueChange: (String) -> Unit, minLines: Int = 1) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = RoundedCornerShape(10.dp),
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LeaderPrimary,
            unfocusedBorderColor = LeaderSecondary.copy(alpha = 0.2f)
        )
    )
}

@Composable
private fun SelectionRow(items: List<Pair<String, String>>, selectedId: String?, onSelected: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { (id, name) ->
            FilterChip(
                selected = selectedId == id,
                onClick = { onSelected(id) },
                label = { Text(name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LeaderPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun LogItem(log: SupplyLog) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = LeaderSurface,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(log.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = LeaderPrimary)
                Text(log.harvestDate, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("From: ${log.familyName}", style = MaterialTheme.typography.bodySmall, color = CharcoalInk)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(color = LeaderHighlight, shape = RoundedCornerShape(4.dp)) {
                Text(
                    text = "BATCH: ${log.batchId}",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = LeaderSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeaderTraceabilityViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    private val productRepository: ProductRepository,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val sessionState = observeSessionUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    private val _formState = MutableStateFlow(LeaderTraceabilityFormState())
    val formState = _formState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    val uiState = sessionState
        .flatMapLatest { session ->
            val loggedIn = session as? SessionState.LoggedIn
            val cooperativeId = loggedIn?.cooperativeId
            if (cooperativeId == null) {
                flowOf(LeaderTraceabilityUiState.Error("Missing cooperative access"))
            } else {
                combine(
                    productRepository.getProducts(),
                    observeFamilies(cooperativeId),
                    observeSupplyLogs(cooperativeId),
                ) { products, families, logs ->
                    LeaderTraceabilityUiState.Content(products, families, logs) as LeaderTraceabilityUiState
                }
            }
        }
        .catch { emit(LeaderTraceabilityUiState.Error(it.localizedMessage ?: "Unable to load traceability controls")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaderTraceabilityUiState.Loading)

    fun updateFamilyName(value: String) = updateForm { copy(familyName = value) }
    fun updateVillage(value: String) = updateForm { copy(village = value) }
    fun updateDistrict(value: String) = updateForm { copy(district = value) }
    fun updateRegion(value: String) = updateForm { copy(region = value) }
    fun updateStory(value: String) = updateForm { copy(story = value) }
    fun updateCraft(value: String) = updateForm { copy(primaryCraft = value) }
    fun selectProduct(productId: String) = updateForm { copy(selectedProductId = productId) }
    fun selectFamily(familyId: String) = updateForm { copy(selectedFamilyId = familyId) }
    fun updateQuantity(value: String) = updateForm { copy(quantity = value) }
    fun updateHarvestDate(value: String) = updateForm { copy(harvestDate = value) }
    fun updateNotes(value: String) = updateForm { copy(notes = value) }

    fun saveFamily() {
        val session = sessionState.value as? SessionState.LoggedIn ?: return
        val current = _formState.value
        if (current.familyName.isBlank() || current.village.isBlank()) {
            viewModelScope.launch { _events.emit("Family name and village are required") }
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val family = TribalFamily(
                familyId = current.familyName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "family-$now" },
                cooperativeId = session.cooperativeId ?: return@launch,
                familyName = current.familyName.trim(),
                village = current.village.trim(),
                district = current.district.trim(),
                forestRegion = current.region.trim(),
                story = current.story.trim(),
                primaryCraft = current.primaryCraft.trim(),
                isActive = true,
                createdAt = now,
                updatedAt = now,
            )
            runCatching { upsertFamily(family) }
                .onSuccess {
                    _events.emit("Family saved.")
                    updateForm { copy(familyName = "", village = "", district = "", region = "", story = "", primaryCraft = "") }
                }
                .onFailure { _events.emit(it.localizedMessage ?: "Unable to save family") }
        }
    }

    fun createSupplyLog() {
        val session = sessionState.value as? SessionState.LoggedIn ?: return
        val content = uiState.value as? LeaderTraceabilityUiState.Content ?: return
        val current = _formState.value
        val product = content.products.firstOrNull { it.productId == current.selectedProductId }
        val family = content.families.firstOrNull { it.familyId == current.selectedFamilyId }
        if (product == null || family == null) {
            viewModelScope.launch { _events.emit("Select both product and family") }
            return
        }
        val quantity = current.quantity.toIntOrNull()
        if (quantity == null || quantity <= 0 || current.harvestDate.isBlank()) {
            viewModelScope.launch { _events.emit("Quantity and harvest date are required") }
            return
        }
        viewModelScope.launch {
            runCatching {
                createSupplyLogInternal(
                    cooperativeId = session.cooperativeId ?: return@runCatching,
                    productId = product.productId,
                    productName = product.name,
                    family = family,
                    quantity = quantity,
                    unit = product.unit,
                    harvestDate = current.harvestDate.trim(),
                    originVillage = family.village,
                    notes = current.notes.trim(),
                )
            }.onSuccess {
                _events.emit("Supply log recorded.")
                updateForm { copy(selectedProductId = null, selectedFamilyId = null, quantity = "", harvestDate = "", notes = "") }
            }.onFailure {
                _events.emit(it.localizedMessage ?: "Unable to record supply log")
            }
        }
    }

    private fun updateForm(transform: LeaderTraceabilityFormState.() -> LeaderTraceabilityFormState) {
        _formState.value = _formState.value.transform()
    }

    private fun observeFamilies(cooperativeId: String) = callbackFlow<List<TribalFamily>> {
        val listener = firestore.collection("tribal_families")
            .whereEqualTo("cooperativeId", cooperativeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toFamily() }.orEmpty().sortedBy { it.familyName }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    private fun observeSupplyLogs(cooperativeId: String) = callbackFlow<List<SupplyLog>> {
        val listener = firestore.collection("supply_logs")
            .whereEqualTo("cooperativeId", cooperativeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toSupplyLog() }.orEmpty().sortedByDescending { it.createdAt }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    private suspend fun upsertFamily(family: TribalFamily) {
        awaitTask {
            firestore.collection("tribal_families").document(family.familyId).set(
                mapOf(
                    "familyId" to family.familyId,
                    "cooperativeId" to family.cooperativeId,
                    "familyName" to family.familyName,
                    "village" to family.village,
                    "district" to family.district,
                    "forestRegion" to family.forestRegion,
                    "story" to family.story,
                    "primaryCraft" to family.primaryCraft,
                    "isActive" to family.isActive,
                    "createdAt" to family.createdAt,
                    "updatedAt" to family.updatedAt,
                ),
            )
        }
    }

    private suspend fun createSupplyLogInternal(
        cooperativeId: String,
        productId: String,
        productName: String,
        family: TribalFamily,
        quantity: Int,
        unit: String,
        harvestDate: String,
        originVillage: String,
        notes: String,
    ) {
        val now = System.currentTimeMillis()
        val batchId = "${productName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "batch" }}-$now"
        val logId = "$batchId-log"
        awaitTask {
            firestore.runBatch { batch ->
                batch.set(
                    firestore.collection("supply_logs").document(logId),
                    mapOf(
                        "logId" to logId,
                        "cooperativeId" to cooperativeId,
                        "productId" to productId,
                        "productName" to productName,
                        "familyId" to family.familyId,
                        "familyName" to family.familyName,
                        "batchId" to batchId,
                        "quantity" to quantity,
                        "unit" to unit,
                        "harvestDate" to harvestDate,
                        "originVillage" to originVillage,
                        "notes" to notes,
                        "createdAt" to now,
                    ),
                )
                batch.set(
                    firestore.collection("batch_records").document(batchId),
                    mapOf(
                        "batchId" to batchId,
                        "cooperativeId" to cooperativeId,
                        "productId" to productId,
                        "productName" to productName,
                        "familyId" to family.familyId,
                        "familyName" to family.familyName,
                        "harvestDate" to harvestDate,
                        "originVillage" to originVillage,
                        "quantity" to quantity,
                        "unit" to unit,
                        "status" to "RECORDED",
                        "createdAt" to now,
                        "updatedAt" to now,
                    ),
                )
            }
        }
    }

    private suspend fun <T> awaitTask(block: () -> com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            block()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}

sealed interface LeaderTraceabilityUiState {
    data object Loading : LeaderTraceabilityUiState
    data class Error(val message: String) : LeaderTraceabilityUiState
    data class Content(
        val products: List<Product>,
        val families: List<TribalFamily>,
        val logs: List<SupplyLog>,
    ) : LeaderTraceabilityUiState
}

data class LeaderTraceabilityFormState(
    val familyName: String = "",
    val village: String = "",
    val district: String = "",
    val region: String = "",
    val story: String = "",
    val primaryCraft: String = "",
    val selectedProductId: String? = null,
    val selectedFamilyId: String? = null,
    val quantity: String = "",
    val harvestDate: String = "",
    val notes: String = "",
)

private fun com.google.firebase.firestore.DocumentSnapshot.toFamily(): TribalFamily? {
    val familyId = getString("familyId") ?: id
    val cooperativeId = getString("cooperativeId") ?: return null
    return TribalFamily(
        familyId = familyId,
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
    val logId = getString("logId") ?: id
    val cooperativeId = getString("cooperativeId") ?: return null
    return SupplyLog(
        logId = logId,
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

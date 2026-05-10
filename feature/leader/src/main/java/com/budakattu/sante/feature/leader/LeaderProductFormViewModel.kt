package com.budakattu.sante.feature.leader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.model.ProductDraft
import com.budakattu.sante.domain.usecase.product.AddProductUseCase
import com.budakattu.sante.domain.usecase.product.GetProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class LeaderProductFormViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow(LeaderProductFormUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init {
        if (productId != null) {
            loadProduct(productId)
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            val product = getProductUseCase(id).firstOrNull()
            if (product != null) {
                _uiState.value = LeaderProductFormUiState(
                    name = product.name,
                    categoryName = product.categoryName,
                    description = product.description,
                    audioDescription = product.audioDescription,
                    quantity = product.availableStock.toString(),
                    unit = product.unit,
                    price = product.pricePerUnit.toString(),
                    msp = product.mspPerUnit.toString(),
                    season = product.season.orEmpty(),
                    familyName = product.familyName,
                    village = product.village,
                    imageUrl = product.imageUrls.firstOrNull().orEmpty(),
                    expectedDispatch = product.expectedDispatchDate.orEmpty(),
                    prebookLimit = product.preorderLimit.toString(),
                    availability = product.availability,
                    isPrebookEnabled = product.isPrebookEnabled,
                    // Additional fields
                    harvestDate = product.addedAt.toString(), // Simplified for now
                    batchNumber = product.productId,
                    isVisible = product.isAvailable
                )
            }
        }
    }

    fun updateName(value: String) = updateState { copy(name = value, errorMessage = null) }
    fun updateCategory(value: String) = updateState { copy(categoryName = value, errorMessage = null) }
    fun updateDescription(value: String) = updateState { copy(description = value, errorMessage = null) }
    fun updateAudioDescription(value: String) = updateState { copy(audioDescription = value, errorMessage = null) }
    fun updateQuantity(value: String) = updateState { copy(quantity = value, errorMessage = null) }
    fun updateUnit(value: String) = updateState { copy(unit = value, errorMessage = null) }
    fun updatePrice(value: String) = updateState { copy(price = value, errorMessage = null) }
    fun updateMsp(value: String) = updateState { copy(msp = value, errorMessage = null) }
    fun updateSeason(value: String) = updateState { copy(season = value, errorMessage = null) }
    fun updateFamilyName(value: String) = updateState { copy(familyName = value, errorMessage = null) }
    fun updateVillage(value: String) = updateState { copy(village = value, errorMessage = null) }
    fun updateImageUrl(value: String) = updateState { copy(imageUrl = value, errorMessage = null) }
    fun updateExpectedDispatch(value: String) = updateState { copy(expectedDispatch = value, errorMessage = null) }
    fun updatePrebookLimit(value: String) = updateState { copy(prebookLimit = value, errorMessage = null) }
    fun updateAvailability(value: ProductAvailability) = updateState { copy(availability = value, errorMessage = null) }
    fun updatePrebookEnabled(value: Boolean) = updateState { copy(isPrebookEnabled = value, errorMessage = null) }

    // New visual-only fields for the refined UI
    fun updateHarvestDate(value: String) = updateState { copy(harvestDate = value) }
    fun updateExpiryDate(value: String) = updateState { copy(expiryDate = value) }
    fun updateBatchNumber(value: String) = updateState { copy(batchNumber = value) }
    fun updateIsVisible(value: Boolean) = updateState { copy(isVisible = value) }
    fun updateNotifyBuyers(value: Boolean) = updateState { copy(notifyBuyers = value) }

    fun saveDraft() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true, errorMessage = null)
            runCatching {
                addProductUseCase(
                    ProductDraft(
                        name = current.name.trim(),
                        categoryId = current.categoryName.trim().lowercase().replace(" ", "-"),
                        categoryName = current.categoryName.trim(),
                        description = current.description.trim(),
                        audioDescription = current.audioDescription.trim(),
                        familyName = current.familyName.trim(),
                        village = current.village.trim(),
                        pricePerUnit = current.price.toFloatOrNull() ?: 0f,
                        mspPerUnit = current.msp.toFloatOrNull() ?: 0f,
                        unit = current.unit.trim(),
                        availableStock = current.quantity.toIntOrNull() ?: 0,
                        season = current.season.trim().takeIf { it.isNotBlank() },
                        imageUrls = listOfNotNull(current.imageUrl.trim().takeIf { it.isNotBlank() }),
                        availability = current.availability,
                        isPrebookEnabled = current.isPrebookEnabled,
                        expectedDispatchDate = current.expectedDispatch.trim().takeIf { it.isNotBlank() },
                        preorderLimit = current.prebookLimit.toIntOrNull() ?: 0,
                    ),
                    isDraft = true,
                    productId = productId
                )
            }.onSuccess {
                _uiState.value = current.copy(isSaving = false)
                _events.emit("Draft saved successfully.")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.localizedMessage ?: "Unable to save draft",
                )
            }
        }
    }

    fun autofillAudioDescription() {
        val current = _uiState.value
        val generated = buildString {
            append(current.name.ifBlank { "This product" })
            if (current.description.isNotBlank()) {
                append(". ")
                append(current.description.trim())
            }
        }.trim()
        _uiState.value = current.copy(audioDescription = generated)
    }

    fun submit() {
        val current = _uiState.value
        val validationError = validate(current)
        if (validationError != null) {
            _uiState.value = current.copy(errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true, errorMessage = null)
            runCatching {
                addProductUseCase(
                    ProductDraft(
                        name = current.name.trim(),
                        categoryId = current.categoryName.trim().lowercase().replace(" ", "-"),
                        categoryName = current.categoryName.trim(),
                        description = current.description.trim(),
                        audioDescription = current.audioDescription.trim(),
                        familyName = current.familyName.trim(),
                        village = current.village.trim(),
                        pricePerUnit = current.price.toFloat(),
                        mspPerUnit = current.msp.toFloat(),
                        unit = current.unit.trim(),
                        availableStock = current.quantity.toIntOrNull() ?: 0,
                        season = current.season.trim().takeIf { it.isNotBlank() },
                        imageUrls = listOfNotNull(current.imageUrl.trim().takeIf { it.isNotBlank() }),
                        availability = current.availability,
                        isPrebookEnabled = current.isPrebookEnabled,
                        expectedDispatchDate = current.expectedDispatch.trim().takeIf { it.isNotBlank() },
                        preorderLimit = current.prebookLimit.toIntOrNull() ?: 0,
                    ),
                    productId = productId
                )
            }.onSuccess {
                _uiState.value = LeaderProductFormUiState(
                    familyName = current.familyName,
                    village = current.village,
                )
                _events.emit("Product added to the marketplace.")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.localizedMessage ?: "Unable to save product",
                )
            }
        }
    }

    private fun validate(state: LeaderProductFormUiState): String? {
        if (state.name.isBlank()) return "Product name is required"
        if (state.categoryName.isBlank()) return "Category is required"
        if (state.description.isBlank()) return "Description is required"
        if (state.quantity.toIntOrNull() == null) return "Enter a valid quantity"
        if (state.price.toFloatOrNull() == null) return "Enter a valid price"
        if (state.msp.toFloatOrNull() == null) return "Enter a valid MSP"
        if (state.familyName.isBlank()) return "Family or collective name is required"
        if (state.village.isBlank()) return "Village is required"
        if (state.isPrebookEnabled && state.expectedDispatch.isBlank()) return "Add an expected dispatch note for pre-booking"
        return null
    }

    private fun updateState(transform: LeaderProductFormUiState.() -> LeaderProductFormUiState) {
        _uiState.value = _uiState.value.transform()
    }
}

data class LeaderProductFormUiState(
    val name: String = "",
    val categoryName: String = "Honey",
    val description: String = "",
    val audioDescription: String = "",
    val quantity: String = "",
    val unit: String = "kg",
    val price: String = "",
    val msp: String = "250",
    val season: String = "",
    val familyName: String = "Koliya Beta Family Group",
    val village: String = "Agumbe, Shimoga, Karnataka",
    val imageUrl: String = "",
    val expectedDispatch: String = "",
    val prebookLimit: String = "",
    val availability: ProductAvailability = ProductAvailability.IN_STOCK,
    val isPrebookEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    
    // Additional fields for visual fidelity
    val harvestDate: String = "25 May 2026",
    val expiryDate: String = "25 Nov 2026",
    val batchNumber: String = "HB-2026-05-001",
    val isVisible: Boolean = true,
    val notifyBuyers: Boolean = false
)

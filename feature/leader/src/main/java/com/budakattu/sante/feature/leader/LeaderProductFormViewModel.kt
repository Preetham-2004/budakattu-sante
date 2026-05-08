package com.budakattu.sante.feature.leader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.ProductAvailability
import com.budakattu.sante.domain.model.ProductDraft
import com.budakattu.sante.domain.usecase.product.AddProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LeaderProductFormViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LeaderProductFormUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

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
                        availableStock = current.quantity.toInt(),
                        season = current.season.trim().takeIf { it.isNotBlank() },
                        imageUrls = listOfNotNull(current.imageUrl.trim().takeIf { it.isNotBlank() }),
                        availability = current.availability,
                        isPrebookEnabled = current.isPrebookEnabled,
                        expectedDispatchDate = current.expectedDispatch.trim().takeIf { it.isNotBlank() },
                        preorderLimit = current.prebookLimit.toIntOrNull() ?: 0,
                    ),
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
    val msp: String = "",
    val season: String = "",
    val familyName: String = "",
    val village: String = "",
    val imageUrl: String = "",
    val expectedDispatch: String = "",
    val prebookLimit: String = "",
    val availability: ProductAvailability = ProductAvailability.IN_STOCK,
    val isPrebookEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

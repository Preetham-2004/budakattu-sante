package com.budakattu.sante.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.TribalFamily
import com.budakattu.sante.domain.repository.TraceabilityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HeritageUiState(
    val families: List<TribalFamily> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HeritageViewModel @Inject constructor(
    private val traceabilityRepository: TraceabilityRepository
) : ViewModel() {

    val uiState: StateFlow<HeritageUiState> = traceabilityRepository
        .observeFamilies("demo-cooperative")
        .map { HeritageUiState(families = it, isLoading = false) }
        .catch { emit(HeritageUiState(isLoading = false)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HeritageUiState()
        )
}

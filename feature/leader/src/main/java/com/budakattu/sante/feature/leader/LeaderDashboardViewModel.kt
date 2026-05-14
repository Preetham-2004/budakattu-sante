package com.budakattu.sante.feature.leader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budakattu.sante.domain.model.OrderStatus
import com.budakattu.sante.domain.model.SessionState
import com.budakattu.sante.domain.usecase.order.ObserveLeaderOrdersUseCase
import com.budakattu.sante.domain.usecase.product.GetProductsUseCase
import com.budakattu.sante.domain.usecase.product.SyncProductsUseCase
import com.budakattu.sante.domain.usecase.session.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LeaderDashboardViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    getProductsUseCase: GetProductsUseCase,
    observeLeaderOrdersUseCase: ObserveLeaderOrdersUseCase,
    private val syncProductsUseCase: SyncProductsUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch {
            syncProductsUseCase()
        }
    }

    val uiState: StateFlow<LeaderDashboardUiState> = observeSessionUseCase()
        .flatMapLatest { session ->
            when (session) {
                is SessionState.LoggedIn -> {
                    val cooperativeId = session.cooperativeId ?: "demo-cooperative"
                    
                    val productsFlow = getProductsUseCase(includeDrafts = true)
                    val ordersFlow = observeLeaderOrdersUseCase(cooperativeId)
                    
                    combine(productsFlow, ordersFlow) { products, orders ->
                        val pendingOrders = orders.count { 
                            it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED 
                        }
                        
                        val mspAlerts = products.count { it.pricePerUnit < it.mspPerUnit }
                        val stockAlerts = products.count { it.availableStock < 5 }
                        val totalAlerts = mspAlerts + stockAlerts
                        
                        val drafts = products.filter { it.isDraft }
                        
                        LeaderDashboardUiState.Success(
                            leaderName = session.name,
                            leaderRole = session.role.name,
                            profilePictureUrl = session.profilePictureUrl,
                            pendingOrdersCount = pendingOrders.toString().padStart(2, '0'),
                            alertsCount = totalAlerts.toString().padStart(2, '0'),
                            syncStatus = "Active",
                            drafts = drafts
                        )
                    }
                }
                SessionState.Loading -> flowOf(LeaderDashboardUiState.Loading)
                is SessionState.LoggedOut -> flowOf(LeaderDashboardUiState.Error("Session expired"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderDashboardUiState.Loading
        )
}

sealed interface LeaderDashboardUiState {
    data object Loading : LeaderDashboardUiState
    data class Success(
        val leaderName: String,
        val leaderRole: String,
        val profilePictureUrl: String?,
        val pendingOrdersCount: String,
        val alertsCount: String,
        val syncStatus: String,
        val drafts: List<com.budakattu.sante.domain.model.Product> = emptyList()
    ) : LeaderDashboardUiState
    data class Error(val message: String) : LeaderDashboardUiState
}

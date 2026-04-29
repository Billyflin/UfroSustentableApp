package com.ecosense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosense.model.RecyclingRequest
import com.ecosense.repository.RecyclingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Success(val requests: List<RecyclingRequest>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

sealed class RedeemState {
    data object Idle    : RedeemState()
    data object Loading : RedeemState()
    data object Success : RedeemState()
    data class Error(val message: String) : RedeemState()
}

class HistoryViewModel(
    private val recyclingRepository: RecyclingRepository = RecyclingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    private val _redeemState = MutableStateFlow<RedeemState>(RedeemState.Idle)
    val redeemState: StateFlow<RedeemState> = _redeemState

    private val _requestDetail = MutableStateFlow<RecyclingRequest?>(null)
    val requestDetail: StateFlow<RecyclingRequest?> = _requestDetail

    fun loadRequests(userId: String) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            recyclingRepository.getRequestsForUser(userId)
                .onSuccess { _uiState.value = HistoryUiState.Success(it) }
                .onFailure { _uiState.value = HistoryUiState.Error(it.message ?: "Error al cargar solicitudes") }
        }
    }

    fun loadRequestDetail(requestId: String) {
        viewModelScope.launch {
            recyclingRepository.getRequestById(requestId)
                .onSuccess { _requestDetail.value = it }
                .onFailure { _requestDetail.value = null }
        }
    }

    fun redeemReward(requestId: String, userId: String, rewardPoints: Int) {
        viewModelScope.launch {
            _redeemState.value = RedeemState.Loading
            recyclingRepository.redeemReward(requestId, userId, rewardPoints)
                .onSuccess { _redeemState.value = RedeemState.Success }
                .onFailure { _redeemState.value = RedeemState.Error(it.message ?: "Error al reclamar la recompensa") }
        }
    }

    fun resetRedeemState() {
        _redeemState.value = RedeemState.Idle
    }
}



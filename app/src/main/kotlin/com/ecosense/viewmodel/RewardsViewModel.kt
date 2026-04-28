package com.ecosense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosense.model.RewardItem
import com.ecosense.repository.RewardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RewardsUiState(
    val rewards: List<RewardItem> = emptyList(),
    val userPoints: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class RewardsViewModel(
    private val rewardsRepository: RewardsRepository = RewardsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState

    fun initialize(userId: String) {
        loadRewards()
        observeUserPoints(userId)
    }

    private fun loadRewards() {
        viewModelScope.launch {
            rewardsRepository.getRewards()
                .onSuccess { rewards ->
                    _uiState.value = _uiState.value.copy(rewards = rewards, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Error al cargar premios",
                        isLoading = false
                    )
                }
        }
    }

    private fun observeUserPoints(userId: String) {
        viewModelScope.launch {
            rewardsRepository.getUserPointsFlow(userId).collect { points ->
                _uiState.value = _uiState.value.copy(userPoints = points)
            }
        }
    }
}


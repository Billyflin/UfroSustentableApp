package com.ecosense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosense.RecyclingPoint
import com.ecosense.repository.MapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val recyclingPoints: List<RecyclingPoint> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MapViewModel(
    private val mapRepository: MapRepository = MapRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        loadRecyclingPoints()
    }

    private fun loadRecyclingPoints() {
        viewModelScope.launch {
            mapRepository.getRecyclingPoints()
                .onSuccess { points ->
                    _uiState.value = MapUiState(recyclingPoints = points, isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = MapUiState(isLoading = false, error = error.message)
                }
        }
    }

    fun retry() {
        _uiState.value = MapUiState(isLoading = true)
        loadRecyclingPoints()
    }
}



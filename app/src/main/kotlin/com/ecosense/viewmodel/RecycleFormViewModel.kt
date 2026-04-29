package com.ecosense.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosense.repository.RecyclingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecycleFormUiState {
    data object Idle     : RecycleFormUiState()
    data object Uploading: RecycleFormUiState()
    data object Success  : RecycleFormUiState()
    data class Error(val message: String) : RecycleFormUiState()
}

class RecycleFormViewModel(
    private val recyclingRepository: RecyclingRepository = RecyclingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecycleFormUiState>(RecycleFormUiState.Idle)
    val uiState: StateFlow<RecycleFormUiState> = _uiState

    fun submitRequest(
        userId: String,
        materialType: String,
        quantityKg: Double,
        image: Bitmap,
        description: String?
    ) {
        viewModelScope.launch {
            _uiState.value = RecycleFormUiState.Uploading
            recyclingRepository.uploadImage(image)
                .onSuccess { photoUrl ->
                    recyclingRepository.createRequest(userId, materialType, quantityKg, photoUrl, description)
                        .onSuccess { _uiState.value = RecycleFormUiState.Success }
                        .onFailure { _uiState.value = RecycleFormUiState.Error(it.message ?: "Error al crear la solicitud") }
                }
                .onFailure { _uiState.value = RecycleFormUiState.Error(it.message ?: "Error al subir la imagen") }
        }
    }

    fun resetState() {
        _uiState.value = RecycleFormUiState.Idle
    }
}



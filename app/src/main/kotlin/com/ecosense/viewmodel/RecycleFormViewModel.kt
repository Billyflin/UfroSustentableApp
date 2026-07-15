package com.ecosense.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosense.repository.RecyclingRepository
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecycleFormUiState {
    data object Idle     : RecycleFormUiState()
    data object Uploading: RecycleFormUiState()
    data class Success(val imageUploaded: Boolean) : RecycleFormUiState()
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
            val uploadResult = recyclingRepository.uploadImage(image, userId)
            val uploadError = uploadResult.exceptionOrNull()
            if (uploadError != null && !uploadError.isMissingStorageBucket()) {
                _uiState.value = RecycleFormUiState.Error(uploadError.message ?: "Error al subir la imagen")
                return@launch
            }

            val photoUrl = uploadResult.getOrNull().orEmpty()
            recyclingRepository.createRequest(userId, materialType, quantityKg, photoUrl, description)
                .onSuccess {
                    _uiState.value = RecycleFormUiState.Success(imageUploaded = photoUrl.isNotEmpty())
                }
                .onFailure {
                    _uiState.value = RecycleFormUiState.Error(it.message ?: "Error al crear la solicitud")
                }
        }
    }

    fun resetState() {
        _uiState.value = RecycleFormUiState.Idle
    }
}

private fun Throwable.isMissingStorageBucket(): Boolean =
    (this as? StorageException)?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND


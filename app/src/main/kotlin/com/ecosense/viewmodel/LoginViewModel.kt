package com.ecosense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosense.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle    : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signInWithGoogle(idToken)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Error al iniciar sesión") }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signInAnonymously()
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Error al iniciar sesión") }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    fun setError(message: String) {
        _uiState.value = LoginUiState.Error(message)
    }
}

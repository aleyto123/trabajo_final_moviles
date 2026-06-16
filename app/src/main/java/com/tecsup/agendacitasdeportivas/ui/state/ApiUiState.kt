package com.tecsup.agendacitasdeportivas.ui.state

// Sealed interface para manejar los estados de las peticiones a internet
sealed interface ApiUiState<out T> {
    object Idle : ApiUiState<Nothing>
    object Loading : ApiUiState<Nothing>
    data class Success<T>(val data: T) : ApiUiState<T>
    data class Error(val message: String) : ApiUiState<Nothing>
}
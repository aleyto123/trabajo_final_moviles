package com.tecsup.agendacitasdeportivas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendacitasdeportivas.data.network.GeminiResponse
import com.tecsup.agendacitasdeportivas.data.network.WeatherResponse
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.ui.state.ApiUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApiViewModel(
    private val repository: CanchaReservationRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<ApiUiState<WeatherResponse>>(ApiUiState.Idle)
    val weatherState: StateFlow<ApiUiState<WeatherResponse>> = _weatherState.asStateFlow()

    private val _geminiState = MutableStateFlow<ApiUiState<GeminiResponse>>(ApiUiState.Idle)
    val geminiState: StateFlow<ApiUiState<GeminiResponse>> = _geminiState.asStateFlow()

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = ApiUiState.Loading
            val response = repository.fetchWeather(lat, lon)
            if (response != null) {
                _weatherState.value = ApiUiState.Success(response)
            } else {
                _weatherState.value = ApiUiState.Error("Error de red: No se pudo obtener el clima.")
            }
        }
    }

    fun askGemini(apiKey: String, prompt: String) {
        viewModelScope.launch {
            _geminiState.value = ApiUiState.Loading
            val response = repository.askGemini(apiKey, prompt)
            if (response != null) {
                _geminiState.value = ApiUiState.Success(response)
            } else {
                _geminiState.value = ApiUiState.Error("Error de red: No se pudo conectar con la IA.")
            }
        }
    }
}
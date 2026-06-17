package com.tecsup.agendacitasdeportivas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendacitasdeportivas.data.network.GroqResponse
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

    private val GROQ_TOKEN = ""

    private val _weatherState = MutableStateFlow<ApiUiState<WeatherResponse>>(ApiUiState.Idle)
    val weatherState: StateFlow<ApiUiState<WeatherResponse>> = _weatherState.asStateFlow()

    private val _groqState = MutableStateFlow<ApiUiState<GroqResponse>>(ApiUiState.Idle)
    val groqState: StateFlow<ApiUiState<GroqResponse>> = _groqState.asStateFlow()

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = ApiUiState.Loading
            repository.fetchWeather(lat, lon).fold(
                onSuccess = { response ->
                    _weatherState.value = ApiUiState.Success(response)
                },
                onFailure = { error ->
                    _weatherState.value = ApiUiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Cielo despejado"
            1, 2, 3 -> "Parcialmente nublado"
            45, 48 -> "Neblina"
            51, 53, 55 -> "Llovizna"
            61, 63, 65 -> "Lluvia"
            71, 73, 75 -> "Nieve"
            77 -> "Granizo"
            80, 81, 82 -> "Chubascos"
            95, 96, 99 -> "Tormenta"
            else -> "Clima variado"
        }
    }

    fun askChatBot(prompt: String) {
        val weatherInfo = when (val s = _weatherState.value) {
            is ApiUiState.Success -> {
                val desc = getWeatherDescription(s.data.current_weather.weathercode)
                "Clima actual en Lima: ${s.data.current_weather.temperature}°C, $desc. "
            }
            else -> "Clima no disponible. "
        }

        val context = """
            Eres un asistente para la app 'Agenda Citas Deportivas'. 
            Responde SOLO sobre: disponibilidad, características de canchas, precios, recomendaciones de horarios y clima.
            
            Información:
            - Estadio Nacional: Fútbol, S/50.
            - Lawn Tennis: Tenis, S/40.
            - Limatambo: Bádminton (Techado), S/30.
            - El Golazo: Fútbol 7, S/45.
            - Horario: 08:00 a 22:00.
            - Clima: $weatherInfo (Si llueve, recomienda Limatambo por ser techado).
            
            Si preguntan algo ajeno, di que no puedes responder.
            Respuesta corta:
        """.trimIndent()

        viewModelScope.launch {
            _groqState.value = ApiUiState.Loading
            repository.askGroq(GROQ_TOKEN, "$context \nUsuario: $prompt").fold(
                onSuccess = { response ->
                    _groqState.value = ApiUiState.Success(response)
                },
                onFailure = { error ->
                    _groqState.value = ApiUiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }
}

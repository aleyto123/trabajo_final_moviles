package com.tecsup.agendacitasdeportivas.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tecsup.agendacitasdeportivas.data.network.GroqResponse
import com.tecsup.agendacitasdeportivas.data.network.WeatherResponse
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.ui.state.ApiUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
        viewModelScope.launch {
            _groqState.value = ApiUiState.Loading

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // 1. Obtener información del clima
            val weatherInfo = when (val s = _weatherState.value) {
                is ApiUiState.Success -> {
                    val desc = getWeatherDescription(s.data.current_weather.weathercode)
                    "Clima actual: ${s.data.current_weather.temperature}°C, $desc."
                }
                else -> "Clima no disponible."
            }

            // 2. Catálogo de canchas
            val allCanchasInfo = CanchaProvider.allCanchas.take(15).joinToString("\n") { 
                "- ${it.name}: ${it.type}, S/${it.pricePerHour}. Ubicación: ${it.address}" 
            }

            // 3. Reservas actuales para contexto y modificación
            val userReservations = repository.allReservations.first()
            val reservationsContext = if (userReservations.isEmpty()) {
                "El usuario no tiene reservas."
            } else {
                "RESERVAS ACTUALES (Usa el ID para modificar):\n" + userReservations.joinToString("\n") { 
                    "- ID: ${it.id}, Cancha: ${it.canchaType}, Fecha: ${it.reservationDate}, Hora: ${it.reservationTime}, Estado: ${it.estado}" 
                }
            }

            val context = """
                Eres el Asistente Pro de 'CanchaLibre'. Hoy es $today.
                
                TIENES PERMISO PARA CREAR Y MODIFICAR RESERVAS.
                
                SISTEMA:
                - Catálogo: $allCanchasInfo
                - Clima: $weatherInfo
                - Reservas del Usuario: $reservationsContext
                
                INSTRUCCIONES DE ACCIÓN:
                - Para CREAR: Si el usuario quiere reservar, tras tu respuesta amable, añade al final: [ACTION:CREATE|Cancha|YYYY-MM-DD|HH:mm|Nombre]
                - Para MODIFICAR: Si quiere cambiar una reserva existente, añade: [ACTION:UPDATE|ID|Cancha|YYYY-MM-DD|HH:mm|Nombre]
                
                REGLAS:
                1. Siempre confirma los datos (Cancha, Fecha, Hora) antes de ejecutar.
                2. Si falta un dato (como la hora), pregúntale al usuario primero.
                3. Responde de forma muy breve y amigable.
                
                Pregunta: $prompt
            """.trimIndent()

            repository.askGroq(GROQ_TOKEN, context).fold(
                onSuccess = { response ->
                    processAiResponse(response)
                },
                onFailure = { error ->
                    _groqState.value = ApiUiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }

    private suspend fun processAiResponse(response: GroqResponse) {
        val text = response.choices.firstOrNull()?.message?.content ?: ""
        
        if (text.contains("[ACTION:")) {
            try {
                val actionPart = text.substringAfter("[ACTION:").substringBefore("]")
                val parts = actionPart.split("|")
                val type = parts[0] // CREATE o UPDATE
                
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "ANONYMOUS"
                val customerName = parts.last()
                
                if (type == "CREATE") {
                    val canchaName = parts[1]
                    val date = parts[2]
                    val time = parts[3]
                    val precio = CanchaProvider.allCanchas.find { it.name == canchaName }?.pricePerHour ?: 50.0
                    
                    val newRes = CanchaReservationEntity(
                        userId = currentUserId,
                        canchaType = canchaName,
                        customerName = customerName,
                        reservationDate = date,
                        reservationTime = time,
                        hourlyPrice = precio,
                        paymentStatus = "Pendiente",
                        estado = "Pendiente"
                    )
                    repository.insertReservation(newRes)
                    Log.d("IA_ACTION", "Reserva creada por IA")
                } else if (type == "UPDATE") {
                    val resId = parts[1]
                    val canchaName = parts[2]
                    val date = parts[3]
                    val time = parts[4]
                    
                    val existing = repository.getReservationById(resId)
                    if (existing != null) {
                        val updatedRes = existing.copy(
                            canchaType = canchaName,
                            reservationDate = date,
                            reservationTime = time,
                            customerName = customerName
                        )
                        repository.updateReservation(updatedRes)
                        Log.d("IA_ACTION", "Reserva modificada por IA")
                    }
                }
            } catch (e: Exception) {
                Log.e("IA_ACTION", "Error procesando acción", e)
            }
        }
        
        // Limpiar los tags de la respuesta para que el usuario no los vea
        val cleanText = text.substringBefore("[ACTION:").trim()
        val cleanResponse = response.copy(
            choices = listOf(response.choices[0].copy(
                message = response.choices[0].message.copy(content = cleanText)
            ))
        )
        _groqState.value = ApiUiState.Success(cleanResponse)
    }
}

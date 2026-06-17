package com.tecsup.agendacitasdeportivas.data.repository

import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationDao
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.ChatResponse
import com.tecsup.agendacitasdeportivas.data.network.RetrofitClient
import com.tecsup.agendacitasdeportivas.data.network.WeatherResponse
import kotlinx.coroutines.flow.Flow
import java.net.UnknownHostException

class CanchaReservationRepository(
    private val canchaReservationDao: CanchaReservationDao
) {
    // --- PERSISTENCIA LOCAL (ROOM) ---
    val allReservations: Flow<List<CanchaReservationEntity>> =
        canchaReservationDao.getAllReservations()

    suspend fun getReservationById(id: String): CanchaReservationEntity? {
        return canchaReservationDao.getReservationById(id)
    }

    suspend fun insertReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.insert(reservation)
    }

    suspend fun updateReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.update(reservation)
    }

    suspend fun deleteReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.delete(reservation)
    }

    // --- CONSUMO DE API CON PROTECCIÓN TRY-CATCH ROBUSTO ---
    suspend fun fetchWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = RetrofitClient.weatherApi.getWeather(lat, lon)
            Result.success(response)
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No hay conexión a internet. Verifique su red."))
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener el clima: ${e.localizedMessage}"))
        }
    }

    suspend fun askChatBot(token: String, prompt: String): Result<ChatResponse> {
        return try {
            // Separar contexto si existe el marcador
            val messages = if (prompt.contains("\nUsuario:")) {
                val parts = prompt.split("\nUsuario:")
                listOf(
                    com.tecsup.agendacitasdeportivas.data.network.ChatMessage(role = "system", content = parts[0].trim()),
                    com.tecsup.agendacitasdeportivas.data.network.ChatMessage(role = "user", content = parts[1].trim())
                )
            } else {
                listOf(
                    com.tecsup.agendacitasdeportivas.data.network.ChatMessage(role = "user", content = prompt)
                )
            }

            val request = com.tecsup.agendacitasdeportivas.data.network.ChatRequest(
                model = "llama-3.1-8b-instant", // Modelo altamente compatible y rápido en Groq
                messages = messages,
                temperature = 0.5
            )
            
            val authHeader = if (token.trim().startsWith("Bearer ")) token.trim() else "Bearer ${token.trim()}"
            val response = RetrofitClient.chatApi.getChatCompletion(authHeader, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Error de ChatBot: ${e.localizedMessage}"))
        }
    }
}
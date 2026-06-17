package com.tecsup.agendacitasdeportivas.data.repository

import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationDao
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.GroqMessage
import com.tecsup.agendacitasdeportivas.data.network.GroqRequest
import com.tecsup.agendacitasdeportivas.data.network.GroqResponse
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

    // --- CONSUMO DE API CLIMA ---
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

    // --- CONSUMO DE API GROQ (AI CHATBOT) ---
    suspend fun askGroq(token: String, prompt: String): Result<GroqResponse> {
        return try {
            val messages = if (prompt.contains("\nUsuario:")) {
                val parts = prompt.split("\nUsuario:")
                listOf(
                    GroqMessage(role = "system", content = parts[0].trim()),
                    GroqMessage(role = "user", content = parts[1].trim())
                )
            } else {
                listOf(GroqMessage(role = "user", content = prompt))
            }

            val request = GroqRequest(
                model = "llama-3.1-8b-instant",
                messages = messages,
                temperature = 0.5
            )
            
            val authHeader = if (token.trim().startsWith("Bearer ")) token.trim() else "Bearer ${token.trim()}"
            val response = RetrofitClient.groqApi.getChatCompletion(authHeader, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Error de ChatBot (Groq): ${e.localizedMessage}"))
        }
    }
}

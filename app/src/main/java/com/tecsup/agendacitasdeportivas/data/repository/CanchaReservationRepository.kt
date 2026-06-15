package com.tecsup.agendacitasdeportivas.data.repository

import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationDao
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.RetrofitClient
import com.tecsup.agendacitasdeportivas.data.network.WeatherResponse
import com.tecsup.agendacitasdeportivas.data.network.GeminiResponse
import kotlinx.coroutines.flow.Flow

class CanchaReservationRepository(
    private val canchaReservationDao: CanchaReservationDao
) {
    // --- PERSISTENCIA LOCAL (ROOM) ---
    val allReservations: Flow<List<CanchaReservationEntity>> =
        canchaReservationDao.getAllReservations()

    suspend fun insertReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.insert(reservation)
    }

    suspend fun updateReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.update(reservation)
    }

    suspend fun deleteReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.delete(reservation)
    }

    // --- CONSUMO DE API CON PROTECCIÓN TRY-CATCH ---
    suspend fun fetchWeather(lat: Double, lon: Double): WeatherResponse? {
        return try {
            RetrofitClient.weatherApi.getWeather(lat, lon)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Retorna null si no hay internet o falla el servidor
        }
    }

    suspend fun askGemini(apiKey: String, prompt: String): GeminiResponse? {
        return try {
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
            )
            RetrofitClient.geminiApi.generateContent(apiKey, requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
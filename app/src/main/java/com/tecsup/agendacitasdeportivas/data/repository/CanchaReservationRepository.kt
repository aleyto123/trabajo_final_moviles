package com.tecsup.agendacitasdeportivas.data.repository

import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationDao
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.RetrofitClient
import com.tecsup.agendacitasdeportivas.data.network.WeatherResponse
import com.tecsup.agendacitasdeportivas.data.network.GeminiResponse
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

    suspend fun askGemini(apiKey: String, prompt: String): Result<GeminiResponse> {
        return try {
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
            )
            val response = RetrofitClient.geminiApi.generateContent(apiKey, requestBody)
            Result.success(response)
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No hay conexión a internet para contactar a la IA."))
        } catch (e: Exception) {
            Result.failure(Exception("Error de IA: ${e.localizedMessage}"))
        }
    }
}
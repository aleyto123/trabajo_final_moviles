package com.tecsup.agendacitasdeportivas.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationDao
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class CanchaReservationRepository(
    private val canchaReservationDao: CanchaReservationDao,
    private val context: android.content.Context
) {
    private val firestore = FirebaseFirestore.getInstance()

    val allReservations: Flow<List<CanchaReservationEntity>> =
        canchaReservationDao.getAllReservations()

    suspend fun getReservationById(id: String): CanchaReservationEntity? {
        return canchaReservationDao.getReservationById(id)
    }

    suspend fun getReservationFromFirestore(id: String): CanchaReservationEntity? {
        return try {
            val snapshot = firestore.collection("reservas").document(id).get().await()
            snapshot.toObject(CanchaReservationEntity::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncLocal(reservation: CanchaReservationEntity) {
        canchaReservationDao.insert(reservation)
    }

    suspend fun insertReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.insert(reservation)
        
        // Notificación local de confirmación de reserva
        com.tecsup.agendacitasdeportivas.ui.utils.NotificationHelper.showNotification(
            context, 
            "Reserva Confirmada", 
            "Tu reserva para ${reservation.canchaType} ha sido registrada correctamente."
        )

        try {
            firestore.collection("reservas").document(reservation.id).set(reservation).await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al sincronizar insert", e)
        }
    }

    suspend fun updateReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.update(reservation)
        
        // Notificación local por modificación
        com.tecsup.agendacitasdeportivas.ui.utils.NotificationHelper.showNotification(
            context, 
            "Reserva Modificada", 
            "Los cambios en tu reserva para ${reservation.canchaType} se guardaron correctamente."
        )

        try {
            firestore.collection("reservas").document(reservation.id).set(reservation).await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al sincronizar update", e)
        }
    }

    suspend fun deleteReservation(reservation: CanchaReservationEntity) {
        canchaReservationDao.delete(reservation)
        try {
            firestore.collection("reservas").document(reservation.id).delete().await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error al sincronizar delete", e)
        }
    }

    suspend fun completePayment(reservation: CanchaReservationEntity): Result<Unit> {
        return try {
            val paidReservation = reservation.copy(
                paymentStatus = "Pagado", 
                estado = "pagado", 
                synced = false
            )
            canchaReservationDao.update(paidReservation)
            firestore.collection("reservas").document(paidReservation.id).set(paidReservation).await()
            canchaReservationDao.update(paidReservation.copy(synced = true))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = RetrofitClient.weatherApi.getWeather(lat, lon)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askGroq(token: String, prompt: String): Result<GroqResponse> {
        return try {
            val messages = if (prompt.contains("\nUsuario:")) {
                val parts = prompt.split("\nUsuario:")
                listOf(GroqMessage("system", parts[0].trim()), GroqMessage("user", parts[1].trim()))
            } else {
                listOf(GroqMessage("user", prompt))
            }
            val request = GroqRequest("llama-3.1-8b-instant", messages, 0.5)
            val authHeader = if (token.trim().startsWith("Bearer ")) token.trim() else "Bearer ${token.trim()}"
            val response = RetrofitClient.groqApi.getChatCompletion(authHeader, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

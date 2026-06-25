package com.tecsup.agendacitasdeportivas.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationDao
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.network.GroqMessage
import com.tecsup.agendacitasdeportivas.data.network.GroqRequest
import com.tecsup.agendacitasdeportivas.data.network.GroqResponse
import com.tecsup.agendacitasdeportivas.data.network.RetrofitClient
import com.tecsup.agendacitasdeportivas.data.network.WeatherResponse
import com.google.auth.oauth2.GoogleCredentials
import com.tecsup.agendacitasdeportivas.data.network.FcmV1Message
import com.tecsup.agendacitasdeportivas.data.network.FcmV1Payload
import com.tecsup.agendacitasdeportivas.data.network.FcmNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

class CanchaReservationRepository(
    private val canchaReservationDao: CanchaReservationDao,
    private val context: android.content.Context
) {
    // Instancia privada de Firestore para sincronización
    private val firestore = FirebaseFirestore.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Obtiene el token OAuth2 usando la cuenta de servicio desde assets
     */
    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val assetManager = context.assets
        val inputStream = assetManager.open("firebase-auth.json")
        val googleCredentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        googleCredentials.refreshIfExpired()
        "Bearer ${googleCredentials.accessToken.tokenValue}"
    }

    /**
     * Implementa un SnapshotListener para detectar cuando el estado de pago cambia a "pagado"
     * y dispara la notificación push remota usando FCM V1.
     */
    fun escucharPagoYDispararPush(idReserva: String, tokenFCMDelCelular: String) {
        // Apuntando a la colección "reservas" como solicita el usuario
        val docRef = firestore.collection("reservas").document(idReserva)
        
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FCM_V1", "Error al escuchar Firestore", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Buscando el campo "estado" con valor "pagado"
                val estado = snapshot.getString("estado")
                if (estado == "pagado") {
                    Log.d("FCM_V1", "¡Pago detectado en Firestore! Enviando notificación...")
                    
                    repositoryScope.launch {
                        try {
                            val oauthToken = getAccessToken()
                            
                            val payload = FcmV1Payload(
                                message = FcmV1Message(
                                    token = tokenFCMDelCelular,
                                    notification = FcmNotification(
                                        title = "¡Reserva Confirmada!",
                                        body = "Tu cancha ya está lista para el partido"
                                    )
                                )
                            )

                            val response = RetrofitClient.fcmApi.sendNotificationV1(oauthToken, payload)
                            if (response.isSuccessful) {
                                Log.d("FCM_V1", "Push enviado con éxito mediante API V1")
                            } else {
                                Log.e("FCM_V1", "Error en API V1: ${response.errorBody()?.string()}")
                            }
                        } catch (ex: Exception) {
                            Log.e("FCM_V1", "Excepción al enviar push V1", ex)
                        }
                    }
                }
            }
        }
    }

    // --- PERSISTENCIA LOCAL (ROOM) ---
    val allReservations: Flow<List<CanchaReservationEntity>> =
        canchaReservationDao.getAllReservations()

    suspend fun getReservationById(id: String): CanchaReservationEntity? {
        return canchaReservationDao.getReservationById(id)
    }

    /**
     * Inserta una reserva localmente en Room y luego la sincroniza con Firebase Firestore.
     */
    suspend fun insertReservation(reservation: CanchaReservationEntity) {
        // 1. Guardado local en Room mediante el DAO
        canchaReservationDao.insert(reservation)

        // 2. Disparar notificación local
        com.tecsup.agendacitasdeportivas.ui.utils.NotificationHelper.showNotification(
            context,
            "Reserva Local",
            "Cancha guardada con éxito en tu historial local"
        )

        // 3. Sincronización automática en la nube (Firestore)
        try {
            firestore.collection("reservas")
                .document(reservation.id)
                .set(reservation)
                .addOnSuccessListener {
                    Log.d("Firestore", "Reserva ${reservation.id} sincronizada correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al sincronizar la reserva ${reservation.id}", e)
                }
        } catch (e: Exception) {
            Log.e("Firestore", "Excepción al intentar conectar con Firestore", e)
        }
    }

    suspend fun updateReservation(reservation: CanchaReservationEntity) {
        // 1. Actualización local
        canchaReservationDao.update(reservation)

        // 2. Disparar notificación local por actualización
        com.tecsup.agendacitasdeportivas.ui.utils.NotificationHelper.showNotification(
            context,
            "Reserva Actualizada",
            "Cancha actualizada con éxito en tu historial local"
        )

        // 3. Sincronización de actualización en Firestore
        try {
            firestore.collection("reservas")
                .document(reservation.id)
                .set(reservation)
                .addOnSuccessListener {
                    Log.d("Firestore", "Reserva ${reservation.id} actualizada en la nube")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al actualizar la reserva ${reservation.id}", e)
                }
        } catch (e: Exception) {
            Log.e("Firestore", "Excepción al actualizar en Firestore", e)
        }
    }

    suspend fun deleteReservation(reservation: CanchaReservationEntity) {
        // 1. Eliminación local
        canchaReservationDao.delete(reservation)

        // 2. Eliminación en Firestore
        try {
            firestore.collection("reservas")
                .document(reservation.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Reserva ${reservation.id} eliminada de la nube")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al eliminar la reserva ${reservation.id}", e)
                }
        } catch (e: Exception) {
            Log.e("Firestore", "Excepción al eliminar en Firestore", e)
        }
    }

    suspend fun completePayment(reservation: CanchaReservationEntity): Result<Unit> {
        return try {
            val paidReservation = reservation.copy(
                paymentStatus = "Pagado",
                isSynced = false 
            )
            canchaReservationDao.update(paidReservation)
            
            firestore.collection("reservas")
                .document(paidReservation.id)
                .set(paidReservation)
                .await()

            canchaReservationDao.update(paidReservation.copy(isSynced = true))
            
            // Enviar Notificación PUSH directamente a este dispositivo
            try {
                val fcmToken = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                
                val notificationBody = com.tecsup.agendacitasdeportivas.data.network.FcmPayload(
                    to = fcmToken, // Solo al que pagó
                    notification = com.tecsup.agendacitasdeportivas.data.network.FcmNotification(
                        title = "¡Reserva Confirmada!",
                        body = "Tu pago para la cancha ${reservation.canchaType} fue exitoso."
                    )
                )

                // IMPORTANTE: Aquí se requiere la Server Key de Firebase (Legacy)
                // Se encuentra en: Firebase Console -> Configuración -> Mensajería en la nube
                RetrofitClient.fcmApi.sendNotification(
                    "key=AAAAO0Z_F8Q:APA91bF9X..." , // Reemplazar con tu Server Key real
                    notificationBody
                )
                
                Log.d("FCM_PUSH", "Push enviado correctamente al token: $fcmToken")
            } catch (e: Exception) {
                Log.e("FCM_PUSH", "Error al enviar el push: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CONSUMO DE API CLIMA ---
    suspend fun fetchWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = RetrofitClient.weatherApi.getWeather(lat, lon)
            Result.success(response)
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No hay conexión a internet. Verifique su red."))
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()}). Intente más tarde."))
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
        } catch (e: UnknownHostException) {
            Result.failure(Exception("No hay conexión a internet para el ChatBot. Verifique su red e intente de nuevo."))
        } catch (e: HttpException) {
            Result.failure(Exception("Error de servicio ChatBot (${e.code()}). Verifique su conexión o intente más tarde."))
        } catch (e: Exception) {
            Result.failure(Exception("Error de ChatBot (Groq): ${e.localizedMessage}"))
        }
    }
}

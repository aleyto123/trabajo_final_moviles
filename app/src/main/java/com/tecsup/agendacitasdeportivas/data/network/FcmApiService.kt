package com.tecsup.agendacitasdeportivas.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmApiService {
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") serverKey: String,
        @Body payload: FcmPayload
    ): retrofit2.Response<Unit>

    // FCM V1
    @POST("v1/projects/canchalibre-6d670/messages:send")
    suspend fun sendNotificationV1(
        @Header("Authorization") bearerToken: String,
        @Body payload: FcmV1Payload
    ): retrofit2.Response<Unit>
}

// Modelos para FCM V1
data class FcmV1Payload(
    val message: FcmV1Message
)

data class FcmV1Message(
    val token: String,
    val notification: FcmNotification
)

data class FcmPayload(
    val to: String, // "/topics/reservas"
    val notification: FcmNotification,
    val data: Map<String, String> = emptyMap()
)

data class FcmNotification(
    val title: String,
    val body: String,
    val sound: String = "default"
)

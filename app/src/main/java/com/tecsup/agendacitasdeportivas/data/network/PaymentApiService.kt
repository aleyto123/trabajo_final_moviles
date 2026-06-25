package com.tecsup.agendacitasdeportivas.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PaymentApiService {
    @POST("checkout/preferences")
    suspend fun createPreference(
        @Header("Authorization") token: String,
        @Body request: MPPreferenceRequest
    ): MPPreferenceResponse
}

data class MPPreferenceRequest(
    val items: List<MPItem>,
    val back_urls: MPBackUrls,
    val auto_return: String = "approved",
    val external_reference: String
)

data class MPItem(
    val title: String,
    val quantity: Int = 1,
    val unit_price: Double,
    val currency_id: String = "PEN"
)

data class MPBackUrls(
    val success: String,
    val pending: String,
    val failure: String
)

data class MPPreferenceResponse(
    val id: String,
    val init_point: String // Esta es la URL de pago para el navegador
)

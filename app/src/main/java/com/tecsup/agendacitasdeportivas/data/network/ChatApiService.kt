package com.tecsup.agendacitasdeportivas.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse
}

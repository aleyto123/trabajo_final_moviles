package com.tecsup.agendacitasdeportivas.data.network

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<GroqMessage>,
    @SerializedName("temperature")
    val temperature: Double = 0.7
)

data class GroqMessage(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

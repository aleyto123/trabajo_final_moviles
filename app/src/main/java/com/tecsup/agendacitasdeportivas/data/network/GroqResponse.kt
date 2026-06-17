package com.tecsup.agendacitasdeportivas.data.network

import com.google.gson.annotations.SerializedName

data class GroqResponse(
    @SerializedName("choices")
    val choices: List<GroqChoice>
)

data class GroqChoice(
    @SerializedName("message")
    val message: GroqMessage
)

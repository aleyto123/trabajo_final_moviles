package com.tecsup.agendacitasdeportivas.data.network

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("choices")
    val choices: List<Choice>
)

data class Choice(
    @SerializedName("message")
    val message: ChatMessage
)

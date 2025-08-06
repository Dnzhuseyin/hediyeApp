package com.example.hediyeapp.data

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String = "mixtral-8x7b-32768",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.8,
    @SerializedName("max_tokens") val maxTokens: Int = 1024
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String,
    val choices: List<ChatChoice>,
    val usage: Usage?
)

data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    @SerializedName("finish_reason") val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

data class GiftRecommendation(
    val title: String,
    val description: String,
    val price: String,
    val link: String = ""
) 
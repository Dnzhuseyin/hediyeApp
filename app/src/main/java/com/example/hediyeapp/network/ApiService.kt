package com.example.hediyeapp.network

import com.example.hediyeapp.data.ChatRequest
import com.example.hediyeapp.data.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

object ApiConstants {
    const val BASE_URL = "https://api.groq.com/"
    // TODO: API key'i güvenli bir şekilde environment variable'dan alın
    // Geçici olarak buraya kendi API key'inizi ekleyin
    const val API_KEY = "YOUR_GROQ_API_KEY_HERE"
} 
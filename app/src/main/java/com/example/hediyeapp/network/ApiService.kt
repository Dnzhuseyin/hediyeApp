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
    
    // ⚠️ ÖNEMLİ: Buraya kendi Groq API key'inizi yazın!
    // Groq Console'dan (https://console.groq.com/) ücretsiz API key alabilirsiniz
    // Örnek: "gsk_abcd1234efgh5678ijkl9012mnop3456qrst7890uvwx1234yz5678ab"
    const val API_KEY = "YOUR_GROQ_API_KEY_HERE"
    
    // API key'in doğru ayarlanıp ayarlanmadığını kontrol et
    fun isApiKeyValid(): Boolean {
        return API_KEY != "YOUR_GROQ_API_KEY_HERE" && 
               API_KEY.isNotBlank() && 
               API_KEY.startsWith("gsk_")
    }
} 
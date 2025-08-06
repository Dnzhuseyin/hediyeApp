package com.example.hediyeapp.network

import com.example.hediyeapp.data.GeminiRequest
import com.example.hediyeapp.data.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("v1beta/models/gemini-1.5-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

object ApiConstants {
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    // ⚠️ ÖNEMLİ: Buraya kendi Google Gemini API key'inizi yazın!
    // Google AI Studio'dan (https://aistudio.google.com/) ücretsiz API key alabilirsiniz
    // Örnek: "AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    const val API_KEY = "AIzaSyAApSD6QiKTx-OsgkhewBWhTnk8ScesBP0"
    
    // API key'in doğru ayarlanıp ayarlanmadığını kontrol et
    fun isApiKeyValid(): Boolean {
        return API_KEY != "YOUR_GEMINI_API_KEY_HERE" && 
               API_KEY.isNotBlank() && 
               API_KEY.startsWith("AIza")
    }
} 
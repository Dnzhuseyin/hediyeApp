package com.example.hediyeapp.repository

import com.example.hediyeapp.data.*
import com.example.hediyeapp.network.ApiConstants
import com.example.hediyeapp.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GiftRepository {
    private val apiService = NetworkClient.apiService
    private val gson = Gson()

    suspend fun getGiftRecommendations(answers: List<UserAnswer>): Result<List<GiftRecommendation>> {
        return withContext(Dispatchers.IO) {
            try {
                // API key kontrolü
                if (!ApiConstants.isApiKeyValid()) {
                    return@withContext Result.failure(
                        Exception("❌ API Key Hatası: Lütfen ApiService.kt dosyasında API_KEY değerini kendi Groq API key'iniz ile değiştirin. Groq Console'dan (https://console.groq.com/) ücretsiz API key alabilirsiniz.")
                    )
                }
                
                val userProfile = buildUserProfile(answers)
                val prompt = buildPrompt(userProfile)
                
                val request = ChatRequest(
                    messages = listOf(
                        ChatMessage("system", "Sen bir hediye önerme uzmanısın. Kullanıcının verdiği bilgilerden yola çıkarak ona 3 uygun hediye öner. Yanıtını JSON formatında ver: [{\"title\": \"hediye adı\", \"description\": \"açıklama\", \"price\": \"fiyat aralığı\", \"link\": \"alışveriş linki (varsa)\"}]"),
                        ChatMessage("user", prompt)
                    )
                )

                val response = apiService.getChatCompletion(
                    authorization = "Bearer ${ApiConstants.API_KEY}",
                    request = request
                )

                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    val content = chatResponse?.choices?.firstOrNull()?.message?.content ?: ""
                    
                    if (content.isEmpty()) {
                        return@withContext Result.failure(
                            Exception("❌ API Yanıt Hatası: Groq API'den boş yanıt geldi. API key'inizi kontrol edin.")
                        )
                    }
                    
                    // Try to parse JSON response
                    val recommendations = try {
                        parseRecommendations(content)
                    } catch (e: Exception) {
                        // If JSON parsing fails, create fallback recommendations from text
                        parseTextToRecommendations(content)
                    }
                    
                    if (recommendations.isEmpty()) {
                        return@withContext Result.failure(
                            Exception("❌ Parse Hatası: API yanıtından hediye önerisi çıkarılamadı.")
                        )
                    }
                    
                    Result.success(recommendations)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "❌ Yetkilendirme Hatası (401): API key'iniz geçersiz. Lütfen Groq Console'dan yeni bir key alın."
                        402 -> "❌ Ödeme Hatası (402): API kredi limiti aşıldı. Groq Console'da kredi durumunuzu kontrol edin."
                        429 -> "❌ Rate Limit Hatası (429): Çok fazla istek gönderdiniz. Biraz bekleyip tekrar deneyin."
                        500 -> "❌ Sunucu Hatası (500): Groq API'de geçici sorun. Biraz bekleyip tekrar deneyin."
                        else -> "❌ API Hatası: ${response.code()} - ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("UnknownHostException") == true -> 
                        "❌ İnternet Bağlantısı Hatası: İnternet bağlantınızı kontrol edin."
                    e.message?.contains("timeout") == true -> 
                        "❌ Zaman Aşımı Hatası: İnternet bağlantınız yavaş olabilir. Tekrar deneyin."
                    else -> "❌ Genel Hata: ${e.message}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }

    private fun buildUserProfile(answers: List<UserAnswer>): Map<String, Any> {
        val profile = mutableMapOf<String, Any>()
        val questions = QuestionsData.questions

        answers.forEach { answer ->
            val question = questions.find { it.id == answer.questionId }
            question?.let { q ->
                when (q.type) {
                    QuestionType.TEXT_INPUT -> {
                        profile[q.text] = answer.textInput
                    }
                    QuestionType.MULTIPLE_CHOICE -> {
                        profile[q.text] = answer.selectedOptions
                    }
                    QuestionType.SINGLE_CHOICE -> {
                        profile[q.text] = answer.selectedOptions.firstOrNull() ?: ""
                    }
                }
            }
        }
        return profile
    }

    private fun buildPrompt(userProfile: Map<String, Any>): String {
        val sb = StringBuilder()
        userProfile.forEach { (key, value) ->
            when (value) {
                is List<*> -> sb.append("$key: ${value.joinToString(", ")}\n")
                else -> sb.append("$key: $value\n")
            }
        }
        
        sb.append("\nBana 3 yaratıcı ve kişiselleştirilmiş hediye önerisi ver. Her öneri için başlık, açıklama, tahmini fiyat aralığı ve mümkünse alışveriş linki içersin.")
        return sb.toString()
    }

    private fun parseRecommendations(content: String): List<GiftRecommendation> {
        // Try to extract JSON from content
        val jsonStart = content.indexOf('[')
        val jsonEnd = content.lastIndexOf(']')
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            val jsonString = content.substring(jsonStart, jsonEnd + 1)
            val type = object : TypeToken<List<GiftRecommendation>>() {}.type
            return gson.fromJson(jsonString, type)
        }
        
        throw Exception("JSON formatı bulunamadı")
    }

    private fun parseTextToRecommendations(content: String): List<GiftRecommendation> {
        // Fallback parsing for non-JSON responses
        val recommendations = mutableListOf<GiftRecommendation>()
        val lines = content.split('\n').filter { it.trim().isNotEmpty() }
        
        var currentTitle = ""
        var currentDescription = ""
        var currentPrice = "Fiyat belirtilmemiş"
        
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.matches(Regex("\\d+\\..*")) -> {
                    // Save previous recommendation
                    if (currentTitle.isNotEmpty()) {
                        recommendations.add(
                            GiftRecommendation(
                                title = currentTitle,
                                description = currentDescription,
                                price = currentPrice
                            )
                        )
                    }
                    // Start new recommendation
                    currentTitle = trimmed.substringAfter('.').trim()
                    currentDescription = ""
                    currentPrice = "Fiyat belirtilmemiş"
                }
                trimmed.isNotEmpty() && currentTitle.isNotEmpty() -> {
                    currentDescription += " $trimmed"
                }
            }
        }
        
        // Add the last recommendation
        if (currentTitle.isNotEmpty()) {
            recommendations.add(
                GiftRecommendation(
                    title = currentTitle,
                    description = currentDescription,
                    price = currentPrice
                )
            )
        }
        
        return recommendations.take(3)
    }
} 
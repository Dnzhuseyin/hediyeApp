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
                    
                    // Try to parse JSON response
                    val recommendations = try {
                        parseRecommendations(content)
                    } catch (e: Exception) {
                        // If JSON parsing fails, create fallback recommendations from text
                        parseTextToRecommendations(content)
                    }
                    
                    Result.success(recommendations)
                } else {
                    Result.failure(Exception("API hatası: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
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
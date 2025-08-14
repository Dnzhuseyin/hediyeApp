package com.example.hediyeapp.repository

import com.example.hediyeapp.data.GiftRecommendation
import com.example.hediyeapp.data.GeminiRequest
import com.example.hediyeapp.data.GeminiContent
import com.example.hediyeapp.data.GeminiPart
import com.example.hediyeapp.data.Question
import com.example.hediyeapp.data.UserAnswer
import com.example.hediyeapp.network.ApiConstants
import com.example.hediyeapp.network.NetworkClient
import com.example.hediyeapp.network.WebScrapingService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

class GiftRepository {

    private val apiService = NetworkClient.apiService
    private val webScrapingService = WebScrapingService()

    suspend fun getGiftRecommendations(
        answers: List<UserAnswer>,
        questions: List<Question>
    ): Result<List<GiftRecommendation>> = withContext(Dispatchers.IO) {
        try {
            // API key kontrolü
            if (!ApiConstants.isApiKeyValid()) {
                return@withContext Result.failure(
                    Exception("❌ API key geçersiz! Lütfen doğru Gemini API key'i girin.")
                )
            }

            // Kullanıcı profilini oluştur
            val userProfile = buildUserProfile(answers, questions)
            
            // AI için prompt hazırla
            val prompt = buildGiftPrompt(userProfile)
            
            // Gemini API request oluştur
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            // API çağrısı yap
            val response = apiService.generateContent(
                apiKey = ApiConstants.API_KEY,
                request = request
            )

            when {
                response.isSuccessful -> {
                    val geminiResponse = response.body()
                    val content = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                    if (content.isNullOrBlank()) {
                        Result.failure(Exception("⚠️ AI'dan boş yanıt alındı. Lütfen tekrar deneyin."))
                    } else {
                        // JSON parsing dene, başarısız olursa text parsing yap
                        val initialRecommendations = try {
                            parseJsonResponse(content)
                        } catch (e: Exception) {
                            parseTextResponse(content)
                        }

                        if (initialRecommendations.isEmpty()) {
                            Result.failure(Exception("⚠️ Hediye önerileri bulunamadı. Lütfen tekrar deneyin."))
                        } else {
                            // Web scraping ile gerçek satın alma linklerini bul
                            val enhancedRecommendations = enhanceWithRealLinks(initialRecommendations)
                            Result.success(enhancedRecommendations)
                        }
                    }
                }
                else -> {
                    val errorMessage = when (response.code()) {
                        400 -> "❌ Geçersiz istek! API formatında sorun var."
                        401 -> "🔑 API key geçersiz! Lütfen doğru Gemini API key'i kontrol edin."
                        403 -> "🚫 API erişimi engellendi! API key'inizin yetkisi var mı?"
                        429 -> "⏱️ API limiti aşıldı! Birkaç dakika bekleyip tekrar deneyin."
                        500, 502, 503 -> "🔧 Gemini servisi geçici olarak kullanılamıyor. Lütfen tekrar deneyin."
                        else -> "🌐 API hatası (${response.code()}): ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            }

        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("⏱️ Bağlantı zaman aşımı! İnternet bağlantınızı kontrol edin."))
        } catch (e: TimeoutException) {
            Result.failure(Exception("⏱️ İstek zaman aşımına uğradı! Lütfen tekrar deneyin."))
        } catch (e: IOException) {
            Result.failure(Exception("🌐 İnternet bağlantısı hatası! Bağlantınızı kontrol edin."))
        } catch (e: Exception) {
            Result.failure(Exception("❌ Beklenmeyen hata: ${e.message ?: "Bilinmeyen hata"}"))
        }
    }

    // Web scraping ile gerçek satın alma linklerini ekle
    private suspend fun enhanceWithRealLinks(recommendations: List<GiftRecommendation>): List<GiftRecommendation> = withContext(Dispatchers.IO) {
        try {
            // Paralel olarak tüm ürünler için link ara
            val enhancedRecommendations = recommendations.map { recommendation ->
                async {
                    webScrapingService.searchProductLinks(recommendation)
                }
            }.awaitAll()
            
            return@withContext enhancedRecommendations
        } catch (e: Exception) {
            // Hata durumunda orijinal önerileri döndür
            return@withContext recommendations
        }
    }

    // Tek bir ürün için gerçek zamanlı link arama
    suspend fun findRealTimeProductLink(recommendation: GiftRecommendation): GiftRecommendation = withContext(Dispatchers.IO) {
        return@withContext webScrapingService.searchProductLinks(recommendation)
    }

    private fun buildUserProfile(answers: List<UserAnswer>, questions: List<Question>): String {
        val profile = StringBuilder()
        
        answers.forEach { answer ->
            val question = questions.find { it.id == answer.questionId }
            question?.let { q ->
                profile.append("${q.text}: ")
                when {
                    answer.selectedOptions.isNotEmpty() -> {
                        profile.append(answer.selectedOptions.joinToString(", "))
                    }
                    answer.textInput.isNotBlank() -> {
                        profile.append(answer.textInput)
                    }
                }
                profile.append("\n")
            }
        }
        
        return profile.toString()
    }

    private fun buildGiftPrompt(userProfile: String): String {
        return """
Sen bir hediye önerim uzmanısın. Aşağıdaki kullanıcı bilgilerine göre 3 mükemmel hediye önerisi yap.

KULLANICI PROFİLİ:
$userProfile

GÖREVLER:
1. Kullanıcı profiline uygun 3 hediye öner
2. Her hediye için net bir başlık, detaylı açıklama, yaklaşık fiyat ver
3. Yanıtını MUTLAKA aşağıdaki JSON formatında ver:

[
  {
    "title": "Hediye Adı",
    "description": "Detaylı açıklama (neden bu hediye perfect, nasıl kullanılır, ne için özel)",
    "price": "₺200-300 arası",
    "link": ""
  },
  {
    "title": "İkinci Hediye",
    "description": "İkinci hediye için detaylı açıklama",
    "price": "₺150-250 arası", 
    "link": ""
  },
  {
    "title": "Üçüncü Hediye",
    "description": "Üçüncü hediye için detaylı açıklama",
    "price": "₺100-200 arası",
    "link": ""
  }
]

ÖNEMLİ: 
- Yanıtın sadece JSON array olsun, başka metin ekleme!
- Link alanını boş bırak, gerçek linkler otomatik olarak bulunacak
- Türkiye'de satılan gerçek ürünler öner
        """.trimIndent()
    }

    private fun parseJsonResponse(content: String): List<GiftRecommendation> {
        return try {
            // JSON'u temizle
            val cleanJson = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val gson = Gson()
            val type = object : TypeToken<List<GiftRecommendation>>() {}.type
            gson.fromJson<List<GiftRecommendation>>(cleanJson, type) ?: emptyList()
        } catch (e: Exception) {
            throw Exception("JSON parsing hatası: ${e.message}")
        }
    }

    private fun parseTextResponse(content: String): List<GiftRecommendation> {
        // Text parsing fallback implementation
        val recommendations = mutableListOf<GiftRecommendation>()
        
        try {
            // Basit text parsing
            val lines = content.lines()
            var currentTitle = ""
            var currentDescription = ""
            var currentPrice = "Fiyat belirtilmemiş"
            var currentLink = "" // Boş bırak, web scraping ile doldurulacak
            
            lines.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") -> {
                        // Önceki hediyeyi kaydet
                        if (currentTitle.isNotBlank()) {
                            recommendations.add(
                                GiftRecommendation(
                                    title = currentTitle,
                                    description = currentDescription.ifBlank { "Harika bir hediye seçeneği" },
                                    price = currentPrice,
                                    link = currentLink
                                )
                            )
                        }
                        // Yeni hediye başlat
                        currentTitle = trimmed.substring(2).trim()
                        currentDescription = ""
                    }
                    trimmed.isNotBlank() && currentTitle.isNotBlank() -> {
                        currentDescription += " $trimmed"
                    }
                }
            }
            
            // Son hediyeyi ekle
            if (currentTitle.isNotBlank()) {
                recommendations.add(
                    GiftRecommendation(
                        title = currentTitle,
                        description = currentDescription.ifBlank { "Harika bir hediye seçeneği" },
                        price = currentPrice,
                        link = currentLink
                    )
                )
            }
            
            // Eğer hiç hediye bulunamazsa varsayılan öneriler ekle
            if (recommendations.isEmpty()) {
                recommendations.addAll(getDefaultRecommendations())
            }
            
        } catch (e: Exception) {
            recommendations.addAll(getDefaultRecommendations())
        }
        
        return recommendations
    }

    private fun getDefaultRecommendations(): List<GiftRecommendation> {
        return listOf(
            GiftRecommendation(
                title = "Kişiselleştirilmiş Hediye",
                description = "Sevdiklerinize özel, kişiselleştirilmiş bir hediye hazırlayın. İsim, fotoğraf veya özel mesaj ekleyebileceğiniz ürünler her zaman değerlidir.",
                price = "₺100-300 arası",
                link = "" // Web scraping ile doldurulacak
            ),
            GiftRecommendation(
                title = "Deneyim Hediyesi",
                description = "Spa günü, konser bileti, yemek kursu veya seyahat gibi unutulmaz anılar yaratacak deneyimler hediye edin.",
                price = "₺200-500 arası", 
                link = "" // Web scraping ile doldurulacak
            ),
            GiftRecommendation(
                title = "Hobiye Yönelik Hediye",
                description = "Sevdiklerinizin hobileri ve ilgi alanlarına uygun, kaliteli bir hediye seçin. Bu her zaman doğru tercih olacaktır.",
                price = "₺150-400 arası",
                link = "" // Web scraping ile doldurulacak
            )
        )
    }
} 
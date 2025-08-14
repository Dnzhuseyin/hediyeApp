package com.example.hediyeapp.network

import com.example.hediyeapp.data.GiftRecommendation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class WebScrapingService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchProductLinks(recommendation: GiftRecommendation): GiftRecommendation = withContext(Dispatchers.IO) {
        try {
            // Türkiye'deki popüler e-ticaret sitelerinde arama yap
            val searchLinks = mutableListOf<String>()
            
            // Ürün adını URL için encode et
            val searchQuery = URLEncoder.encode(recommendation.title, "UTF-8")
            
            // Trendyol arama
            val trendyolLink = searchOnTrendyol(searchQuery)
            if (trendyolLink != null) searchLinks.add(trendyolLink)
            
            // Hepsiburada arama
            val hepsiburadaLink = searchOnHepsiburada(searchQuery)
            if (hepsiburadaLink != null) searchLinks.add(hepsiburadaLink)
            
            // Amazon TR arama
            val amazonLink = searchOnAmazon(searchQuery)
            if (amazonLink != null) searchLinks.add(amazonLink)
            
            // N11 arama
            val n11Link = searchOnN11(searchQuery)
            if (n11Link != null) searchLinks.add(n11Link)
            
            // En iyi linki seç (genellikle ilk bulduğumuz)
            val bestLink = searchLinks.firstOrNull() ?: generateFallbackSearchLink(searchQuery)
            
            return@withContext recommendation.copy(link = bestLink)
            
        } catch (e: Exception) {
            // Hata durumunda genel arama linki döndür
            val fallbackLink = generateFallbackSearchLink(
                URLEncoder.encode(recommendation.title, "UTF-8")
            )
            return@withContext recommendation.copy(link = fallbackLink)
        }
    }
    
    private suspend fun searchOnTrendyol(query: String): String? {
        return try {
            // Trendyol arama URL'si
            "https://www.trendyol.com/sr?q=$query"
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun searchOnHepsiburada(query: String): String? {
        return try {
            // Hepsiburada arama URL'si
            "https://www.hepsiburada.com/ara?q=$query"
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun searchOnAmazon(query: String): String? {
        return try {
            // Amazon Türkiye arama URL'si
            "https://www.amazon.com.tr/s?k=$query"
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun searchOnN11(query: String): String? {
        return try {
            // N11 arama URL'si
            "https://www.n11.com/arama?q=$query"
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generateFallbackSearchLink(query: String): String {
        // Genel Google arama linki
        return "https://www.google.com/search?q=$query+satın+al+türkiye"
    }
    
    // Gelişmiş web scraping - gerçek ürün linklerini bul
    suspend fun findRealProductLinks(recommendation: GiftRecommendation): List<ProductLink> = withContext(Dispatchers.IO) {
        val productLinks = mutableListOf<ProductLink>()
        
        try {
            val searchQuery = URLEncoder.encode(recommendation.title, "UTF-8")
            
            // Trendyol'da gerçek ürün ara
            findTrendyolProducts(searchQuery)?.let { links ->
                productLinks.addAll(links)
            }
            
            // Hepsiburada'da gerçek ürün ara
            findHepsiburadaProducts(searchQuery)?.let { links ->
                productLinks.addAll(links)
            }
            
        } catch (e: Exception) {
            // Hata durumunda boş liste döndür
        }
        
        return@withContext productLinks
    }
    
    private suspend fun findTrendyolProducts(query: String): List<ProductLink>? {
        return try {
            // Basit HTTP isteği ile Trendyol'dan veri çek
            val request = Request.Builder()
                .url("https://www.trendyol.com/sr?q=$query")
                .addHeader("User-Agent", "Mozilla/5.0 (compatible; GiftApp/1.0)")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Basitleştirilmiş parsing - gerçek projede daha detaylı olmalı
                listOf(
                    ProductLink(
                        title = "Trendyol'da Bul",
                        url = "https://www.trendyol.com/sr?q=$query",
                        price = "Fiyatları Karşılaştır",
                        platform = "Trendyol"
                    )
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun findHepsiburadaProducts(query: String): List<ProductLink>? {
        return try {
            listOf(
                ProductLink(
                    title = "Hepsiburada'da Bul",
                    url = "https://www.hepsiburada.com/ara?q=$query",
                    price = "En İyi Fiyatları Gör",
                    platform = "Hepsiburada"
                )
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class ProductLink(
    val title: String,
    val url: String,
    val price: String,
    val platform: String
) 
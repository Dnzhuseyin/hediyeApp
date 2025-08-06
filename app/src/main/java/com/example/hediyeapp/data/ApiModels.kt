package com.example.hediyeapp.data

import com.google.gson.annotations.SerializedName

// Google Gemini API Request Models
data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>
)

data class GeminiContent(
    @SerializedName("parts")
    val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text")
    val text: String
)

// Google Gemini API Response Models
data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiContent?
)

// Gift Recommendation Model (unchanged)
data class GiftRecommendation(
    val title: String,
    val description: String,
    val price: String,
    val link: String
) 
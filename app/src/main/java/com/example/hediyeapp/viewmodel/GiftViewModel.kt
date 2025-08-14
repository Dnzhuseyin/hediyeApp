package com.example.hediyeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hediyeapp.data.*
import com.example.hediyeapp.repository.GiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GiftViewModel : ViewModel() {
    private val repository = GiftRepository()
    
    private val _uiState = MutableStateFlow(GiftUiState())
    val uiState: StateFlow<GiftUiState> = _uiState.asStateFlow()
    
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    private val _answers = MutableStateFlow<Map<Int, UserAnswer>>(emptyMap())
    val answers: StateFlow<Map<Int, UserAnswer>> = _answers.asStateFlow()
    
    // Satın alma butonlarının yükleme durumu
    private val _purchaseButtonStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val purchaseButtonStates: StateFlow<Map<String, Boolean>> = _purchaseButtonStates.asStateFlow()
    
    val questions = QuestionsData.questions
    
    fun answerQuestion(questionId: Int, selectedOptions: List<String> = emptyList(), textInput: String = "") {
        val answer = UserAnswer(
            questionId = questionId,
            selectedOptions = selectedOptions,
            textInput = textInput
        )
        
        _answers.value = _answers.value + (questionId to answer)
    }
    
    fun nextQuestion() {
        if (_currentQuestionIndex.value < questions.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }
    
    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }
    
    fun canGoNext(): Boolean {
        val currentQuestion = questions[_currentQuestionIndex.value]
        val answer = _answers.value[currentQuestion.id]
        
        return when (currentQuestion.type) {
            QuestionType.TEXT_INPUT -> answer?.textInput?.isNotBlank() == true
            QuestionType.SINGLE_CHOICE -> answer?.selectedOptions?.isNotEmpty() == true
            QuestionType.MULTIPLE_CHOICE -> answer?.selectedOptions?.isNotEmpty() == true
        }
    }
    
    fun getRecommendations() {
        if (_answers.value.size != questions.size) {
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            val answersList = _answers.value.values.toList()
            val result = repository.getGiftRecommendations(answersList, questions)
            
            result.fold(
                onSuccess = { recommendations ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendations = recommendations,
                        isCompleted = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Gerçek zamanlı link arama - butona tıklandığında çalışır
    fun findRealTimeLink(recommendation: GiftRecommendation, onLinkFound: (String) -> Unit) {
        val buttonKey = recommendation.title
        
        // Buton yükleme durumunu aktif et
        _purchaseButtonStates.value = _purchaseButtonStates.value + (buttonKey to true)
        
        viewModelScope.launch {
            try {
                val enhancedRecommendation = repository.findRealTimeProductLink(recommendation)
                
                // Buton yükleme durumunu pasif et
                _purchaseButtonStates.value = _purchaseButtonStates.value + (buttonKey to false)
                
                // Gerçek linki kullan
                onLinkFound(enhancedRecommendation.link)
                
                // Öneriyi güncelle
                updateRecommendationLink(recommendation.title, enhancedRecommendation.link)
                
            } catch (e: Exception) {
                // Hata durumunda yükleme durumunu pasif et
                _purchaseButtonStates.value = _purchaseButtonStates.value + (buttonKey to false)
                
                // Fallback link kullan
                val fallbackLink = "https://www.google.com/search?q=${recommendation.title}+satın+al+türkiye"
                onLinkFound(fallbackLink)
            }
        }
    }
    
    // Öneri listesindeki bir öğenin linkini güncelle
    private fun updateRecommendationLink(title: String, newLink: String) {
        val currentRecommendations = _uiState.value.recommendations
        val updatedRecommendations = currentRecommendations.map { recommendation ->
            if (recommendation.title == title) {
                recommendation.copy(link = newLink)
            } else {
                recommendation
            }
        }
        
        _uiState.value = _uiState.value.copy(recommendations = updatedRecommendations)
    }
    
    // Butonun yükleme durumunu kontrol et
    fun isPurchaseButtonLoading(recommendationTitle: String): Boolean {
        return _purchaseButtonStates.value[recommendationTitle] ?: false
    }
    
    fun resetApp() {
        _uiState.value = GiftUiState()
        _currentQuestionIndex.value = 0
        _answers.value = emptyMap()
        _purchaseButtonStates.value = emptyMap()
    }
    
    fun getCurrentQuestion(): Question? {
        return if (_currentQuestionIndex.value < questions.size) {
            questions[_currentQuestionIndex.value]
        } else {
            null
        }
    }
    
    fun getAnswerForQuestion(questionId: Int): UserAnswer? {
        return _answers.value[questionId]
    }
}

data class GiftUiState(
    val isLoading: Boolean = false,
    val recommendations: List<GiftRecommendation> = emptyList(),
    val error: String? = null,
    val isCompleted: Boolean = false
) 
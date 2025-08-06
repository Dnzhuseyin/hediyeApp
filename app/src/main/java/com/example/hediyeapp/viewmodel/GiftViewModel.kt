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
            val result = repository.getGiftRecommendations(answersList)
            
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
                        error = error.message ?: "Bilinmeyen hata"
                    )
                }
            )
        }
    }
    
    fun resetApp() {
        _uiState.value = GiftUiState()
        _currentQuestionIndex.value = 0
        _answers.value = emptyMap()
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
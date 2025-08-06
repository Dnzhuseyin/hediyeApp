package com.example.hediyeapp.data

data class Question(
    val id: Int,
    val text: String,
    val options: List<String> = emptyList(),
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val isRequired: Boolean = true
)

enum class QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TEXT_INPUT
}

data class UserAnswer(
    val questionId: Int,
    val selectedOptions: List<String> = emptyList(),
    val textInput: String = ""
) 
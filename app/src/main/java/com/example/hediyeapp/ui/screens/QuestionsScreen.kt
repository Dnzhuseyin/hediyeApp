package com.example.hediyeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hediyeapp.data.Question
import com.example.hediyeapp.data.QuestionType
import com.example.hediyeapp.viewmodel.GiftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    viewModel: GiftViewModel,
    onQuestionsCompleted: () -> Unit
) {
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val currentQuestion = viewModel.getCurrentQuestion()
    
    val gradientColors = listOf(
        Color(0xFF6B73FF),
        Color(0xFF9575CD),
        Color(0xFFBA68C8)
    )
    
    if (currentQuestion == null) {
        onQuestionsCompleted()
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = (currentQuestionIndex + 1).toFloat() / viewModel.questions.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Question Counter
                Text(
                    text = "Soru ${currentQuestionIndex + 1} / ${viewModel.questions.size}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Question Text
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = currentQuestion.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Question Content
                when (currentQuestion.type) {
                    QuestionType.SINGLE_CHOICE -> {
                        SingleChoiceQuestion(
                            question = currentQuestion,
                            currentAnswer = answers[currentQuestion.id]?.selectedOptions?.firstOrNull(),
                            onAnswerSelected = { selectedOption ->
                                viewModel.answerQuestion(
                                    questionId = currentQuestion.id,
                                    selectedOptions = listOf(selectedOption)
                                )
                            }
                        )
                    }
                    
                    QuestionType.MULTIPLE_CHOICE -> {
                        MultipleChoiceQuestion(
                            question = currentQuestion,
                            currentAnswers = answers[currentQuestion.id]?.selectedOptions ?: emptyList(),
                            onAnswersSelected = { selectedOptions ->
                                viewModel.answerQuestion(
                                    questionId = currentQuestion.id,
                                    selectedOptions = selectedOptions
                                )
                            }
                        )
                    }
                    
                    QuestionType.TEXT_INPUT -> {
                        TextInputQuestion(
                            question = currentQuestion,
                            currentAnswer = answers[currentQuestion.id]?.textInput ?: "",
                            onAnswerChanged = { text ->
                                viewModel.answerQuestion(
                                    questionId = currentQuestion.id,
                                    textInput = text
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous Button
                if (currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuestion() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color.White, Color.White))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Önceki")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                // Next/Finish Button
                val isLastQuestion = currentQuestionIndex == viewModel.questions.size - 1
                val canProceed = viewModel.canGoNext()
                
                Button(
                    onClick = {
                        if (isLastQuestion) {
                            onQuestionsCompleted()
                        } else {
                            viewModel.nextQuestion()
                        }
                    },
                    enabled = canProceed,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6B73FF),
                        disabledContainerColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text(if (isLastQuestion) "Tamamla" else "Sonraki")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = if (isLastQuestion) "Complete" else "Next"
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleChoiceQuestion(
    question: Question,
    currentAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    Column {
        question.options.forEach { option ->
            val isSelected = currentAnswer == option
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = { onAnswerSelected(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        Color.White.copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onAnswerSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (isSelected) Color(0xFF6B73FF) else Color.White,
                            unselectedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        color = if (isSelected) Color(0xFF6B73FF) else Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceQuestion(
    question: Question,
    currentAnswers: List<String>,
    onAnswersSelected: (List<String>) -> Unit
) {
    Column {
        question.options.forEach { option ->
            val isSelected = currentAnswers.contains(option)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    val newAnswers = if (isSelected) {
                        currentAnswers - option
                    } else {
                        currentAnswers + option
                    }
                    onAnswersSelected(newAnswers)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        Color.White.copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            val newAnswers = if (isSelected) {
                                currentAnswers - option
                            } else {
                                currentAnswers + option
                            }
                            onAnswersSelected(newAnswers)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (isSelected) Color(0xFF6B73FF) else Color.White,
                            uncheckedColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        color = if (isSelected) Color(0xFF6B73FF) else Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TextInputQuestion(
    question: Question,
    currentAnswer: String,
    onAnswerChanged: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentAnswer) }
    
    LaunchedEffect(currentAnswer) {
        text = currentAnswer
    }
    
    OutlinedTextField(
        value = text,
        onValueChange = { 
            text = it
            onAnswerChanged(it)
        },
        placeholder = { 
            Text(
                "Cevabınızı yazın...",
                color = Color.White.copy(alpha = 0.7f)
            ) 
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        maxLines = 3
    )
} 
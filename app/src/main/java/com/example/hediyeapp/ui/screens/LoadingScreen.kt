package com.example.hediyeapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hediyeapp.viewmodel.GiftViewModel

@Composable
fun LoadingScreen(
    viewModel: GiftViewModel,
    onRecommendationsReady: () -> Unit,
    onError: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val gradientColors = listOf(
        Color(0xFF6B73FF),
        Color(0xFF9575CD),
        Color(0xFFBA68C8)
    )
    
    // Trigger API call when screen loads
    LaunchedEffect(Unit) {
        viewModel.getRecommendations()
    }
    
    // Navigate when recommendations are ready
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted && uiState.recommendations.isNotEmpty()) {
            onRecommendationsReady()
        }
    }
    
    // Handle errors
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            onError()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Gift Icon
            AnimatedGiftIcon()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Mükemmel Hediyeleri Buluyorum...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Yapay zeka cevaplarınızı analiz ediyor ve\nsize en uygun hediyeleri seçiyor",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress dots
            AnimatedDots()
            
            // Error message if any
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "Hata: ${uiState.error}",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedGiftIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "gift_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Loading Gift",
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .rotate(rotation),
        tint = Color.White
    )
}

@Composable
private fun AnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_animation")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 200,
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(animatedScale)
                    .background(
                        Color.White,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
} 
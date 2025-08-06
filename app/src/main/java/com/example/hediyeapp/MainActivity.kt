package com.example.hediyeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hediyeapp.navigation.GiftGenieNavigation
import com.example.hediyeapp.ui.theme.HediyeAppTheme
import com.example.hediyeapp.viewmodel.GiftViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HediyeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val viewModel: GiftViewModel = viewModel()
                    GiftGenieNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
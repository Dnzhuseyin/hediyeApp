package com.example.hediyeapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hediyeapp.ui.screens.*
import com.example.hediyeapp.viewmodel.GiftViewModel

@Composable
fun GiftGenieNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: GiftViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onStartQuestions = {
                    navController.navigate(Screen.Questions.route)
                }
            )
        }
        
        composable(Screen.Questions.route) {
            QuestionsScreen(
                viewModel = viewModel,
                onQuestionsCompleted = {
                    navController.navigate(Screen.Loading.route)
                }
            )
        }
        
        composable(Screen.Loading.route) {
            LoadingScreen(
                viewModel = viewModel,
                onRecommendationsReady = {
                    navController.navigate(Screen.Results.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                },
                onError = {
                    // Handle error - could navigate back to questions or show error
                }
            )
        }
        
        composable(Screen.Results.route) {
            ResultsScreen(
                viewModel = viewModel,
                onStartOver = {
                    viewModel.resetApp()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Results.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Questions : Screen("questions")
    object Loading : Screen("loading")
    object Results : Screen("results")
} 
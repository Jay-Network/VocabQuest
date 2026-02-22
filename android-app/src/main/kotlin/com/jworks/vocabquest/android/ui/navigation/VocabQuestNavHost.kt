package com.jworks.vocabquest.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jworks.vocabquest.android.ui.feedback.FeedbackDialog
import com.jworks.vocabquest.android.ui.feedback.FeedbackFAB
import com.jworks.vocabquest.android.ui.feedback.FeedbackViewModel
import com.jworks.vocabquest.android.ui.game.flashcard.FlashcardScreen
import com.jworks.vocabquest.android.ui.game.quiz.QuizScreen
import com.jworks.vocabquest.android.ui.home.HomeScreen
import com.jworks.vocabquest.android.ui.progress.ProgressScreen
import com.jworks.vocabquest.android.ui.shop.ShopScreen
import com.jworks.vocabquest.android.ui.subscription.SubscriptionScreen

@Composable
fun EigoQuestNavHost() {
    val navController = rememberNavController()
    val feedbackViewModel: FeedbackViewModel = hiltViewModel()
    val feedbackUiState by feedbackViewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FeedbackFAB(onClick = { feedbackViewModel.openDialog() })
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoute.Home.route) {
                HomeScreen(
                    onQuizClick = { navController.navigate(NavRoute.Quiz.route) },
                    onFlashcardClick = { navController.navigate(NavRoute.Flashcard.route) },
                    onShopClick = { navController.navigate(NavRoute.Shop.route) },
                    onProgressClick = { navController.navigate(NavRoute.Progress.route) },
                    onSubscriptionClick = { navController.navigate(NavRoute.Subscription.route) }
                )
            }

            composable(NavRoute.Quiz.route) {
                QuizScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoute.Flashcard.route) {
                FlashcardScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoute.Shop.route) {
                ShopScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoute.Progress.route) {
                ProgressScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoute.Subscription.route) {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
        }

        if (feedbackUiState.isDialogOpen) {
            FeedbackDialog(
                onDismiss = { feedbackViewModel.closeDialog() },
                viewModel = feedbackViewModel
            )
        }
    }
}

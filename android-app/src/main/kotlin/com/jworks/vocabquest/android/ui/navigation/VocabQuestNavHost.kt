package com.jworks.vocabquest.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jworks.vocabquest.android.ui.game.flashcard.FlashcardScreen
import com.jworks.vocabquest.android.ui.game.quiz.QuizScreen
import com.jworks.vocabquest.android.ui.home.HomeScreen
import com.jworks.vocabquest.android.ui.progress.ProgressScreen
import com.jworks.vocabquest.android.ui.shop.ShopScreen
import com.jworks.vocabquest.android.ui.subscription.SubscriptionScreen

@Composable
fun VocabQuestNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.route
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
}

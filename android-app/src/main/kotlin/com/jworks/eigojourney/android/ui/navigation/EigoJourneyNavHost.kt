package com.jworks.eigojourney.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jworks.eigojourney.android.ui.feedback.FeedbackDialog
import com.jworks.eigojourney.android.ui.feedback.FeedbackFAB
import com.jworks.eigojourney.android.ui.feedback.FeedbackViewModel
import com.jworks.eigojourney.android.ui.game.flashcard.FlashcardScreen
import com.jworks.eigojourney.android.ui.game.quiz.QuizScreen
import com.jworks.eigojourney.android.ui.collection.CollectionScreen
import com.jworks.eigojourney.android.ui.home.HomeScreen
import com.jworks.eigojourney.android.ui.progress.ProgressScreen
import com.jworks.eigojourney.android.ui.shop.ShopScreen
import com.jworks.eigojourney.android.ui.subscription.SubscriptionScreen

@Composable
fun EigoJourneyNavHost() {
    val navController = rememberNavController()
    val feedbackViewModel: FeedbackViewModel = hiltViewModel()
    val feedbackUiState by feedbackViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        floatingActionButton = {
            if (currentRoute == NavRoute.Home.route) {
                FeedbackFAB(onClick = { feedbackViewModel.openDialog() })
            }
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
                    onSubscriptionClick = { navController.navigate(NavRoute.Subscription.route) },
                    onCollectionClick = { navController.navigate(NavRoute.Collection.route) }
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
            composable(NavRoute.Collection.route) {
                CollectionScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoute.Settings.route) {
                SettingsPlaceholderScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = NavRoute.WordDetail.route,
                arguments = listOf(navArgument("wordId") { type = NavType.IntType })
            ) { backStackEntry ->
                val wordId = backStackEntry.arguments?.getInt("wordId") ?: 0
                WordDetailPlaceholderScreen(wordId = wordId, onBack = { navController.popBackStack() })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPlaceholderScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.jworks.eigojourney.android.ui.theme.Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Settings", color = com.jworks.eigojourney.android.ui.theme.Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = com.jworks.eigojourney.android.ui.theme.Glass.TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = com.jworks.eigojourney.android.ui.theme.Glass.Background)
        )
        Text(
            text = "Settings coming soon",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = com.jworks.eigojourney.android.ui.theme.Glass.TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordDetailPlaceholderScreen(wordId: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.jworks.eigojourney.android.ui.theme.Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Word Detail", color = com.jworks.eigojourney.android.ui.theme.Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = com.jworks.eigojourney.android.ui.theme.Glass.TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = com.jworks.eigojourney.android.ui.theme.Glass.Background)
        )
        Text(
            text = "Word detail for ID: $wordId — coming soon",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = com.jworks.eigojourney.android.ui.theme.Glass.TextSecondary
        )
    }
}

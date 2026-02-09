package com.jworks.vocabquest.android.ui.navigation

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object Quiz : NavRoute("game/quiz")
    data object Flashcard : NavRoute("game/flashcard")
    data object Shop : NavRoute("shop")
    data object Progress : NavRoute("progress")
    data object Settings : NavRoute("settings")
    data object WordDetail : NavRoute("word/{wordId}") {
        fun createRoute(wordId: Int) = "word/$wordId"
    }
}

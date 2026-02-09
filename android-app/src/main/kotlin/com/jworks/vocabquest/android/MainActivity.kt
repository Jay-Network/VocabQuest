package com.jworks.vocabquest.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jworks.vocabquest.android.ui.navigation.VocabQuestNavHost
import com.jworks.vocabquest.android.ui.theme.VocabQuestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocabQuestTheme {
                VocabQuestNavHost()
            }
        }
    }
}

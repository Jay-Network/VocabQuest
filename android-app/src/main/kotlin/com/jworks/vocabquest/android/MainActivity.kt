package com.jworks.vocabquest.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jworks.vocabquest.android.ui.navigation.EigoQuestNavHost
import com.jworks.vocabquest.android.ui.theme.EigoQuestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EigoQuestTheme {
                EigoQuestNavHost()
            }
        }
    }
}

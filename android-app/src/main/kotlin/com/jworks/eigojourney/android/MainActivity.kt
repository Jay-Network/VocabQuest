package com.jworks.eigojourney.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jworks.eigojourney.android.ui.navigation.EigoJourneyNavHost
import com.jworks.eigojourney.android.ui.theme.EigoJourneyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EigoJourneyTheme {
                EigoJourneyNavHost()
            }
        }
    }
}

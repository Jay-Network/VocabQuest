package com.jworks.eigojourney.android.ui.game.flashcard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jworks.eigojourney.android.ui.theme.Glass
import com.jworks.eigojourney.android.ui.theme.GlassCard
import com.jworks.eigojourney.android.ui.theme.GlassCardBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    onBack: () -> Unit,
    viewModel: FlashcardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Flashcards", color = Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = Glass.TextSecondary)
                }
            },
            actions = {
                Text(
                    text = "${state.cardsStudied}/${state.totalCards}",
                    modifier = Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Glass.TextAccent
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Glass.Background)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Glass.Accent
                    )
                }
                state.isFinished -> {
                    SessionComplete(
                        cardsStudied = state.cardsStudied,
                        correctCount = state.correctCount,
                        xpEarned = state.xpEarned,
                        onDone = onBack
                    )
                }
                state.currentWord != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (state.totalCards > 0) {
                            LinearProgressIndicator(
                                progress = { state.cardsStudied.toFloat() / state.totalCards },
                                modifier = Modifier.fillMaxWidth(),
                                color = Glass.Accent,
                                trackColor = Glass.ProgressTrack
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Flashcard
                        GlassCardBright(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val word = state.currentWord!!

                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Glass.TextPrimary
                                )

                                word.phonetic?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "/$it/",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Glass.TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${word.pos} | ${word.cefrLevel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Glass.TextDim
                                )

                                AnimatedVisibility(
                                    visible = state.isRevealed,
                                    enter = fadeIn()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            text = word.definition,
                                            style = MaterialTheme.typography.titleLarge,
                                            textAlign = TextAlign.Center,
                                            color = Glass.TextAccent
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!state.isRevealed) {
                            Button(
                                onClick = { viewModel.reveal() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Glass.Accent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Show Answer")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.answer(1) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Glass.Error.copy(alpha = 0.8f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Again") }
                                Button(
                                    onClick = { viewModel.answer(3) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Glass.Gold.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Hard", color = Glass.Background) }
                                Button(
                                    onClick = { viewModel.answer(4) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Glass.Accent),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Good") }
                                Button(
                                    onClick = { viewModel.answer(5) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Glass.Success.copy(alpha = 0.8f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Easy") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionComplete(
    cardsStudied: Int,
    correctCount: Int,
    xpEarned: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Glass.TextPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val accuracy = if (cardsStudied > 0) (correctCount * 100 / cardsStudied) else 0
                Text("Cards Studied: $cardsStudied", style = MaterialTheme.typography.bodyLarge, color = Glass.TextPrimary)
                Text("Accuracy: $accuracy%", style = MaterialTheme.typography.bodyLarge, color = Glass.TextPrimary)
                Text("XP Earned: $xpEarned", style = MaterialTheme.typography.bodyLarge, color = Glass.TextAccent)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = Glass.Accent),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Done") }
    }
}

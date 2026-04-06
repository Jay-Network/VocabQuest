package com.jworks.eigojourney.android.ui.game.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Vocabulary Quiz", color = Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = Glass.TextSecondary)
                }
            },
            actions = {
                if (state.streak > 1) {
                    Text(
                        text = "${state.streak}x",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = Glass.Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                state.isLocked -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Premium Feature",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Glass.Gold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Quiz mode is available with a Premium subscription. Upgrade to unlock all study modes!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Glass.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Glass.Accent),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Go Back") }
                    }
                }
                state.isFinished -> {
                    QuizComplete(
                        correctCount = state.correctCount,
                        totalQuestions = state.totalQuestions,
                        xpEarned = state.xpEarned,
                        onDone = onBack
                    )
                }
                state.currentQuestion != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { (state.questionNumber + 1).toFloat() / state.totalQuestions },
                            modifier = Modifier.fillMaxWidth(),
                            color = Glass.Accent,
                            trackColor = Glass.ProgressTrack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Question ${state.questionNumber + 1} of ${state.totalQuestions}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Glass.TextDim
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val question = state.currentQuestion!!

                        // Word card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentBorder = true
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = question.word.word,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Glass.TextPrimary
                                )
                                question.word.phonetic?.let {
                                    Text(
                                        text = "/$it/",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Glass.TextSecondary
                                    )
                                }
                                Text(
                                    text = question.word.pos,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Glass.TextDim
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Choose the correct definition:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Glass.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Answer options
                        question.options.forEachIndexed { index, option ->
                            val isSelected = state.selectedAnswer == index
                            val isCorrectAnswer = index == question.correctIndex
                            val hasAnswered = state.selectedAnswer != null

                            val borderColor = when {
                                hasAnswered && isCorrectAnswer -> Glass.Success
                                hasAnswered && isSelected && !isCorrectAnswer -> Glass.Error
                                else -> Glass.CardBorder
                            }

                            val containerColor = when {
                                hasAnswered && isCorrectAnswer -> Glass.SuccessGlow
                                hasAnswered && isSelected && !isCorrectAnswer -> Glass.ErrorGlow
                                else -> Glass.SurfaceOverlay
                            }

                            OutlinedButton(
                                onClick = { viewModel.selectAnswer(index) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                enabled = !hasAnswered,
                                border = BorderStroke(
                                    width = if (hasAnswered && (isCorrectAnswer || isSelected)) 2.dp else 1.dp,
                                    color = borderColor
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = containerColor,
                                    contentColor = Glass.TextPrimary,
                                    disabledContentColor = Glass.TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (state.selectedAnswer != null) {
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Glass.Accent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (state.questionNumber + 1 >= state.totalQuestions) "Finish" else "Next"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizComplete(
    correctCount: Int,
    totalQuestions: Int,
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
        val accuracy = if (totalQuestions > 0) correctCount * 100 / totalQuestions else 0
        val grade = when {
            accuracy >= 90 -> "Excellent!"
            accuracy >= 70 -> "Great Job!"
            accuracy >= 50 -> "Good Effort!"
            else -> "Keep Practicing!"
        }

        Text(
            text = grade,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Glass.TextAccent
        )
        Spacer(modifier = Modifier.height(24.dp))

        GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Score: $correctCount/$totalQuestions ($accuracy%)", style = MaterialTheme.typography.titleLarge, color = Glass.TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("XP Earned: +$xpEarned", style = MaterialTheme.typography.bodyLarge, color = Glass.Success)
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

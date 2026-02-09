package com.jworks.vocabquest.android.ui.game.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vocabulary Quiz") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    if (state.streak > 1) {
                        Text(
                            text = "${state.streak}x",
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Quiz mode is available with a Premium subscription. Upgrade to unlock all study modes!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onBack) {
                            Text("Go Back")
                        }
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
                        // Progress
                        LinearProgressIndicator(
                            progress = { (state.questionNumber + 1).toFloat() / state.totalQuestions },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Question ${state.questionNumber + 1} of ${state.totalQuestions}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val question = state.currentQuestion!!

                        // Word to identify
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = question.word.word,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                question.word.phonetic?.let {
                                    Text(
                                        text = "/$it/",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = question.word.pos,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Choose the correct definition:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Answer options
                        question.options.forEachIndexed { index, option ->
                            val isSelected = state.selectedAnswer == index
                            val isCorrectAnswer = index == question.correctIndex
                            val hasAnswered = state.selectedAnswer != null

                            val borderColor = when {
                                hasAnswered && isCorrectAnswer -> Color(0xFF43A047)
                                hasAnswered && isSelected && !isCorrectAnswer -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outline
                            }

                            val containerColor = when {
                                hasAnswered && isCorrectAnswer -> Color(0xFF43A047).copy(alpha = 0.1f)
                                hasAnswered && isSelected && !isCorrectAnswer -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surface
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
                                    containerColor = containerColor
                                )
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

                        // Next button (shown after answering)
                        if (state.selectedAnswer != null) {
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                modifier = Modifier.fillMaxWidth()
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
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Score: $correctCount/$totalQuestions ($accuracy%)", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("XP Earned: +$xpEarned", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}

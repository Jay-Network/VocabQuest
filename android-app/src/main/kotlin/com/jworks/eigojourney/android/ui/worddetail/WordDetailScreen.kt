package com.jworks.eigojourney.android.ui.worddetail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jworks.eigojourney.android.ui.theme.Glass
import com.jworks.eigojourney.android.ui.theme.GlassCard
import com.jworks.eigojourney.android.ui.theme.GlassCardBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    onBack: () -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Word Detail", color = Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = Glass.TextSecondary)
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
                state.notFound -> {
                    Text(
                        text = "Word not found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Glass.TextSecondary
                    )
                }
                else -> {
                    val word = state.word ?: return@Box
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Word header
                        GlassCardBright(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Glass.TextPrimary
                                )
                                word.phonetic?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "/$it/",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Glass.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TagChip(text = word.pos)
                                    TagChip(text = word.cefrLevel)
                                    TagChip(text = "#${word.frequencyRank}")
                                }
                            }
                        }

                        // Definition
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Definition",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Glass.TextAccent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = word.definition,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Glass.TextPrimary
                                )
                            }
                        }

                        // SRS Status
                        GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = true) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Study Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Glass.TextAccent
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    SrsStatItem("Status", state.srsStateLabel)
                                    SrsStatItem("Accuracy", "${state.accuracyPercent}%")
                                    SrsStatItem("Reviews", "${state.srsCard?.totalReviews ?: 0}")
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (state.srsCard != null) {
                                    LinearProgressIndicator(
                                        progress = { state.srsCard!!.accuracy },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Glass.Accent,
                                        trackColor = Glass.ProgressTrack
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Next review",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Glass.TextDim
                                    )
                                    Text(
                                        text = state.nextReviewLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Glass.TextAccent
                                    )
                                }
                            }
                        }

                        // Examples
                        if (word.examples.isNotEmpty()) {
                            Text(
                                text = "Examples",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Glass.TextPrimary
                            )
                            word.examples.forEach { example ->
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = example.sentence,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Glass.TextPrimary
                                        )
                                        example.context?.let { ctx ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = ctx,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Glass.TextDim
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .background(Glass.AccentDim, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Glass.TextAccent
        )
    }
}

@Composable
private fun SrsStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Glass.TextAccent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Glass.TextDim
        )
    }
}

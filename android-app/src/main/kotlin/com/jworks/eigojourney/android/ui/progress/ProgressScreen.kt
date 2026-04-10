package com.jworks.eigojourney.android.ui.progress

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jworks.eigojourney.android.ui.theme.Glass
import com.jworks.eigojourney.android.ui.theme.GlassCard
import com.jworks.eigojourney.core.domain.model.DailyStatsData
import com.jworks.eigojourney.core.domain.model.StudySession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Progress", color = Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = Glass.TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Glass.Background)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Glass.Accent)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        LevelCard(
                            level = state.profile.level,
                            totalXp = state.profile.totalXp,
                            xpProgress = state.profile.xpProgress
                        )
                    }
                    item {
                        StreakCard(
                            currentStreak = state.profile.currentStreak,
                            longestStreak = state.profile.longestStreak
                        )
                    }
                    item {
                        VocabStatsCard(
                            totalWords = state.totalWords,
                            wordsReviewed = state.wordsReviewed,
                            wordsMastered = state.wordsMastered
                        )
                    }
                    if (state.weeklyStats.isNotEmpty()) {
                        item {
                            Text(
                                text = "This Week",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Glass.TextPrimary
                            )
                        }
                        item { WeeklyActivityCard(stats = state.weeklyStats) }
                    }
                    if (state.recentSessions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Sessions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Glass.TextPrimary
                            )
                        }
                        items(state.recentSessions) { session -> SessionCard(session = session) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCard(level: Int, totalXp: Int, xpProgress: Float) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = true) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Level $level",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextAccent
                )
                Text(
                    text = "$totalXp XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = Glass.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { xpProgress },
                modifier = Modifier.fillMaxWidth(),
                color = Glass.Accent,
                trackColor = Glass.ProgressTrack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(xpProgress * 100).toInt()}% to Level ${level + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = Glass.TextDim
            )
        }
    }
}

@Composable
private fun StreakCard(currentStreak: Int, longestStreak: Int) {
    GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = Glass.Gold.copy(alpha = 0.3f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentStreak",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Glass.Gold
                )
                Text(text = "Day Streak", style = MaterialTheme.typography.bodyMedium, color = Glass.TextSecondary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$longestStreak",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Glass.Gold
                )
                Text(text = "Best Streak", style = MaterialTheme.typography.bodyMedium, color = Glass.TextSecondary)
            }
        }
    }
}

@Composable
private fun VocabStatsCard(totalWords: Int, wordsReviewed: Int, wordsMastered: Int) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Vocabulary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Glass.TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Total Words", value = "$totalWords")
                StatItem(label = "Reviewed", value = "$wordsReviewed")
                StatItem(label = "Mastered", value = "$wordsMastered")
            }
            if (totalWords > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { wordsReviewed.toFloat() / totalWords },
                    modifier = Modifier.fillMaxWidth(),
                    color = Glass.Accent,
                    trackColor = Glass.ProgressTrack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${wordsReviewed * 100 / totalWords}% reviewed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Glass.TextDim
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Glass.TextAccent
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Glass.TextDim)
    }
}

@Composable
private fun WeeklyActivityCard(stats: List<DailyStatsData>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val maxCards = stats.maxOfOrNull { it.cardsReviewed } ?: 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${day.cardsReviewed}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Glass.TextAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val barHeight = if (maxCards > 0)
                            (day.cardsReviewed.toFloat() / maxCards * 60).coerceAtLeast(4f)
                        else 4f
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (day.cardsReviewed > 0) Glass.Accent.copy(alpha = 0.7f)
                                    else Glass.ProgressTrack
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.date.takeLast(5),
                            style = MaterialTheme.typography.labelSmall,
                            color = Glass.TextDim,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val totalXp = stats.sumOf { it.xpEarned }
            val totalCards = stats.sumOf { it.cardsReviewed }
            val totalMinutes = stats.sumOf { it.studyTimeSec } / 60
            Text(
                text = "Week total: $totalCards cards, ${totalXp} XP, ${totalMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = Glass.TextSecondary
            )
        }
    }
}

@Composable
private fun SessionCard(session: StudySession) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = session.gameMode.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextPrimary
                )
                Text(
                    text = "${session.cardsStudied} cards | ${(session.accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Glass.TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${session.xpEarned} XP",
                    style = MaterialTheme.typography.titleSmall,
                    color = Glass.TextAccent,
                    fontWeight = FontWeight.Bold
                )
                val minutes = session.durationSec / 60
                Text(
                    text = "${minutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = Glass.TextDim
                )
            }
        }
    }
}

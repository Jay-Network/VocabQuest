package com.jworks.eigojourney.android.ui.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun HomeScreen(
    onQuizClick: () -> Unit,
    onFlashcardClick: () -> Unit,
    onShopClick: () -> Unit,
    onProgressClick: () -> Unit,
    onSubscriptionClick: () -> Unit = {},
    onCollectionClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Glass.Accent
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Header
                Text(
                    text = "EigoJourney",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextAccent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.totalWords} words ready to learn",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Glass.TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Row
                GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard("Streak", "${state.currentStreak} days")
                        StatCard("Level", "${state.level}")
                        StatCard("J Coins", "${state.coinBalance}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Upgrade banner for free users
                if (!state.isPremium) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = Glass.Gold.copy(alpha = 0.3f),
                        onClick = onSubscriptionClick
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Free Plan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Glass.Gold
                                )
                                Text(
                                    text = "Unlock 10,000 words, quiz mode & more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Glass.TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = onSubscriptionClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Glass.Gold.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Upgrade", color = Glass.Background)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Word of the Day
                state.wordOfTheDay?.let { word ->
                    GlassCardBright(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Word of the Day",
                                style = MaterialTheme.typography.labelMedium,
                                color = Glass.TextAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Glass.TextPrimary
                            )
                            word.phonetic?.let {
                                Text(
                                    text = "/$it/",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Glass.TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = word.definition,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Glass.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${word.pos} | ${word.cefrLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Glass.TextDim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Study Modes
                Text(
                    text = "Study Modes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameModeCard(
                        title = "Quiz",
                        description = "Test your knowledge",
                        modifier = Modifier.weight(1f),
                        onClick = onQuizClick
                    )
                    GameModeCard(
                        title = "Flashcards",
                        description = "Learn & review",
                        modifier = Modifier.weight(1f),
                        onClick = onFlashcardClick
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Vocabulary Levels
                Text(
                    text = "Vocabulary by Level",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        state.wordsByLevel.forEach { (level, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = level, fontWeight = FontWeight.Medium, color = Glass.TextPrimary)
                                Text(text = "$count words", color = Glass.TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Collection
                GameModeCard(
                    title = "Word Collection",
                    description = "Discover & collect words",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCollectionClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(title = "Shop", modifier = Modifier.weight(1f), onClick = onShopClick)
                    ActionCard(title = "Progress", modifier = Modifier.weight(1f), onClick = onProgressClick)
                    ActionCard(title = "Settings", modifier = Modifier.weight(1f), onClick = onSettingsClick)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
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

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier, accentBorder = true, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Glass.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Glass.TextSecondary
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(modifier = modifier, onClick = onClick) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Glass.Gold
        )
    }
}

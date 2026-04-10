package com.jworks.eigojourney.android.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSubscriptionClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Settings", color = Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = Glass.TextSecondary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Glass.Background)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Study section
            SectionHeader("Study")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Goal",
                            style = MaterialTheme.typography.titleMedium,
                            color = Glass.TextPrimary
                        )
                        Text(
                            text = "${state.dailyGoal} cards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Glass.TextAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = state.dailyGoal.toFloat(),
                        onValueChange = { viewModel.updateDailyGoal(it.toInt()) },
                        valueRange = 5f..100f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = Glass.Accent,
                            activeTrackColor = Glass.Accent,
                            inactiveTrackColor = Glass.ProgressTrack
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5", style = MaterialTheme.typography.labelSmall, color = Glass.TextDim)
                        Text("100", style = MaterialTheme.typography.labelSmall, color = Glass.TextDim)
                    }
                }
            }

            // Notifications section
            SectionHeader("Notifications")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Study Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                color = Glass.TextPrimary
                            )
                            Text(
                                text = "Daily reminder to study",
                                style = MaterialTheme.typography.bodySmall,
                                color = Glass.TextSecondary
                            )
                        }
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Glass.Accent,
                                checkedTrackColor = Glass.AccentDim,
                                uncheckedThumbColor = Glass.TextDim,
                                uncheckedTrackColor = Glass.ProgressTrack
                            )
                        )
                    }

                    if (state.notificationsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reminder Time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Glass.TextPrimary
                            )
                            Text(
                                text = formatTime(state.reminderHour, state.reminderMinute),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Glass.TextAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = (state.reminderHour * 60 + state.reminderMinute).toFloat(),
                            onValueChange = { totalMinutes ->
                                val mins = totalMinutes.toInt()
                                viewModel.updateReminderTime(mins / 60, mins % 60)
                            },
                            valueRange = 0f..1439f,
                            colors = SliderDefaults.colors(
                                thumbColor = Glass.Accent,
                                activeTrackColor = Glass.Accent,
                                inactiveTrackColor = Glass.ProgressTrack
                            )
                        )
                    }
                }
            }

            // Account section
            SectionHeader("Account")

            GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onSubscriptionClick) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subscription",
                            style = MaterialTheme.typography.titleMedium,
                            color = Glass.TextPrimary
                        )
                        Text(
                            text = state.planName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isPremium) Glass.Gold else Glass.TextSecondary
                        )
                    }
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleLarge,
                        color = Glass.TextDim
                    )
                }
            }

            // About section
            SectionHeader("About")

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    InfoRow("App", "EigoJourney")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Version", state.appVersion.ifEmpty { "v0.2.2" })
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Developer", "JWorks")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Glass.TextPrimary
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Glass.TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Glass.TextPrimary)
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $period"
}

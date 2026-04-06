package com.jworks.eigojourney.android.ui.subscription

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jworks.eigojourney.android.ui.theme.Glass
import com.jworks.eigojourney.android.ui.theme.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Subscription", color = Glass.TextPrimary) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current plan badge
            val planLabel = if (uiState.isPremium) "Premium" else "Free"
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (uiState.isPremium) Glass.Gold.copy(alpha = 0.5f) else Glass.CardBorder
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Plan",
                        style = MaterialTheme.typography.labelLarge,
                        color = Glass.TextSecondary
                    )
                    Text(
                        text = planLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isPremium) Glass.Gold else Glass.TextPrimary
                    )
                    if (uiState.isPremium) {
                        Text(
                            text = "$4.99/month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Glass.TextDim
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feature comparison header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(modifier = Modifier.width(160.dp)) {
                    Text(
                        text = "Free",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = Glass.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Premium",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Glass.Gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    FeatureRow("Vocabulary", freeText = "500 words", premiumText = "10,000 words")
                    FeatureRow("CEFR Levels", freeText = "A1 only", premiumText = "A1-C2")
                    FeatureRow("Flashcard Mode", free = true, premium = true)
                    FeatureRow("Quiz Mode", free = false, premium = true)
                    FeatureRow("Daily Reviews", freeText = "20/day", premiumText = "Unlimited")
                    FeatureRow("Audio Pronunciation", free = false, premium = true)
                    FeatureRow("J Coin Earning", free = false, premium = true)
                    FeatureRow("Shop Purchases", freeText = "View only", premiumText = "Full")
                    FeatureRow("Progress Tracking", free = true, premium = true)
                    FeatureRow("Spaced Repetition", free = true, premium = true)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!uiState.isPremium) {
                Button(
                    onClick = {
                        val url = "https://portal.tutoringjay.com/subscribe/eigojourney"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Glass.Gold.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = "Upgrade to Premium - $4.99/mo",
                        fontWeight = FontWeight.Bold,
                        color = Glass.Background,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Managed through portal.tutoringjay.com",
                    style = MaterialTheme.typography.labelSmall,
                    color = Glass.TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Button(
                    onClick = {
                        val url = "https://portal.tutoringjay.com/subscription"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Glass.Accent)
                ) {
                    Text(
                        text = "Manage Subscription",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    feature: String,
    free: Boolean = false,
    premium: Boolean = false,
    freeText: String? = null,
    premiumText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            color = Glass.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Row(modifier = Modifier.width(160.dp)) {
            Text(
                text = freeText ?: if (free) "Yes" else "---",
                style = MaterialTheme.typography.bodySmall,
                color = if (free || freeText != null) Glass.TextSecondary else Glass.TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = premiumText ?: if (premium) "Yes" else "---",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Glass.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

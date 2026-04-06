package com.jworks.eigojourney.android.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.jworks.eigojourney.core.domain.model.ShopItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("J Coin Shop", color = Glass.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("<", style = MaterialTheme.typography.titleLarge, color = Glass.TextSecondary)
                }
            },
            actions = {
                Text(
                    text = "${state.balance.displayBalance} J",
                    modifier = Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Glass.Gold
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
                !state.coinsEnabled -> {
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
                            text = "J Coin earning and shop purchases require a Premium subscription.",
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
                state.catalog.isEmpty() -> {
                    Text(
                        text = "Shop is empty. Check back later!",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Glass.TextSecondary
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Category filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryChip(
                                text = "All",
                                isSelected = state.selectedCategory == null,
                                onClick = { viewModel.selectCategory(null) }
                            )
                            state.categories.forEach { category ->
                                CategoryChip(
                                    text = category.displayName,
                                    isSelected = state.selectedCategory == category,
                                    onClick = { viewModel.selectCategory(category) }
                                )
                            }
                        }

                        // Items list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.filteredItems) { item ->
                                ShopItemCard(
                                    item = item,
                                    canAfford = state.balance.displayBalance >= item.cost,
                                    onPurchase = { viewModel.purchaseItem(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Purchase feedback snackbar
            state.purchaseMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Glass.SurfaceOverlay,
                    contentColor = Glass.TextPrimary,
                    action = {
                        TextButton(onClick = { viewModel.dismissMessage() }) {
                            Text("OK", color = Glass.Accent)
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Glass.Accent else Glass.SurfaceOverlay,
            contentColor = if (isSelected) Glass.TextPrimary else Glass.TextSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Glass.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Glass.TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Glass.Gold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onPurchase,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Glass.Accent else Glass.SurfaceOverlay,
                    disabledContainerColor = Glass.SurfaceOverlay,
                    disabledContentColor = Glass.TextDim
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("${item.cost} J")
            }
        }
    }
}

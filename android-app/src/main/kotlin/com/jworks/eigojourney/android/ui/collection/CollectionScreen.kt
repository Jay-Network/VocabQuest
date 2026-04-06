package com.jworks.eigojourney.android.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jworks.eigojourney.android.ui.theme.Glass
import com.jworks.eigojourney.android.ui.theme.GlassCard
import com.jworks.eigojourney.core.domain.model.CollectedWord
import com.jworks.eigojourney.core.domain.model.Rarity
import com.jworks.eigojourney.core.domain.model.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Glass.Background)
    ) {
        TopAppBar(
            title = { Text("Word Collection", color = Glass.TextPrimary) },
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
                .padding(16.dp)
        ) {
            // Stats summary card
            val stats = uiState.stats
            GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = true) {
                Column {
                    Text(
                        text = "${stats.totalCollected} Words Collected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Glass.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RarityStatChip("Common", stats.commonCount, Color(Rarity.COMMON.colorValue))
                        RarityStatChip("Uncommon", stats.uncommonCount, Color(Rarity.UNCOMMON.colorValue))
                        RarityStatChip("Rare", stats.rareCount, Color(Rarity.RARE.colorValue))
                        RarityStatChip("Epic", stats.epicCount, Color(Rarity.EPIC.colorValue))
                        RarityStatChip("Legend", stats.legendaryCount, Color(Rarity.LEGENDARY.colorValue))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CEFR level tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LevelTab("All", stats.totalCollected, null, uiState.selectedLevel) {
                    viewModel.selectLevel(null)
                }
                CollectionViewModel.CEFR_LEVELS.forEach { level ->
                    val count = uiState.levelCounts[level] ?: 0
                    LevelTab(level, count, level, uiState.selectedLevel) {
                        viewModel.selectLevel(level)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rarity filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RarityFilterChip("All", null, uiState.selectedRarityFilter) {
                    viewModel.filterByRarity(null)
                }
                Rarity.entries.forEach { rarity ->
                    RarityFilterChip(rarity.label, rarity, uiState.selectedRarityFilter) {
                        viewModel.filterByRarity(rarity)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Item count
            Text(
                text = "${uiState.filteredItems.size} words",
                style = MaterialTheme.typography.bodyMedium,
                color = Glass.TextDim
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Collection grid
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(color = Glass.Accent, trackColor = Glass.ProgressTrack)
                }
            } else if (uiState.filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No words collected yet.\nStudy to discover new words!",
                        textAlign = TextAlign.Center,
                        color = Glass.TextSecondary
                    )
                }
            } else {
                val chunks = uiState.filteredItems.chunked(3)
                chunks.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            val word = uiState.wordMap[item.wordId]
                            WordCollectionCard(
                                item = item,
                                word = word,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RarityStatChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = color
        )
    }
}

@Composable
private fun LevelTab(
    label: String,
    count: Int,
    level: String?,
    selectedLevel: String?,
    onClick: () -> Unit
) {
    val isSelected = level == selectedLevel
    Text(
        text = "$label ($count)",
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Glass.TextPrimary else Glass.TextSecondary,
        modifier = Modifier
            .background(
                color = if (isSelected) Glass.Accent else Glass.SurfaceOverlay,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun RarityFilterChip(
    label: String,
    rarity: Rarity?,
    selectedRarity: Rarity?,
    onClick: () -> Unit
) {
    val isSelected = rarity == selectedRarity
    val chipColor = if (rarity != null) Color(rarity.colorValue) else Glass.Accent
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Glass.TextPrimary else chipColor,
        modifier = Modifier
            .background(
                color = if (isSelected) chipColor.copy(alpha = 0.3f) else Glass.SurfaceOverlay,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun WordCollectionCard(
    item: CollectedWord,
    word: Word?,
    modifier: Modifier = Modifier
) {
    val rarityColor = Color(item.rarity.colorValue)
    val displayWord = word?.word ?: "???"

    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Glass.SurfaceOverlay)
            .border(2.dp, rarityColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        // Word text centered with phonetic reading below
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayWord,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Glass.TextPrimary
            )
            if (word?.phonetic != null) {
                Text(
                    text = "/${word.phonetic}/",
                    fontSize = 9.sp,
                    color = Glass.TextDim,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (word != null) {
                Text(
                    text = word.pos.uppercase(),
                    fontSize = 7.sp,
                    color = Glass.TextDim,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Rarity label (top-left)
        Text(
            text = item.rarity.label.first().toString(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = rarityColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        )

        // Level badge (top-right)
        Text(
            text = "Lv.${item.itemLevel}",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = rarityColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )

        // CEFR badge (bottom-left)
        if (word != null) {
            Text(
                text = word.cefrLevel,
                fontSize = 7.sp,
                color = Glass.TextDim,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 6.dp)
            )
        }

        // XP progress bar at bottom
        if (!item.isMaxLevel) {
            LinearProgressIndicator(
                progress = { item.levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                color = rarityColor,
                trackColor = Color.Transparent
            )
        }
    }
}

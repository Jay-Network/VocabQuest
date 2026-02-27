package com.jworks.vocabquest.android.ui.collection

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.jworks.vocabquest.core.domain.model.CollectedWord
import com.jworks.vocabquest.core.domain.model.Rarity
import com.jworks.vocabquest.core.domain.model.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Collection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("\u2190", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stats summary card
            val stats = uiState.stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${stats.totalCollected} Words Collected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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

            // CEFR level tabs (primary navigation)
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

            // Rarity filter chips (secondary filter)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    LinearProgressIndicator()
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 3-column grid for English words (wider than single kanji)
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
        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
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
    val chipColor = if (rarity != null) Color(rarity.colorValue) else MaterialTheme.colorScheme.primary
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color.White else chipColor,
        modifier = Modifier
            .background(
                color = if (isSelected) chipColor else MaterialTheme.colorScheme.surfaceVariant,
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

    Card(
        modifier = modifier
            .height(96.dp)
            .border(2.dp, rarityColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    overflow = TextOverflow.Ellipsis
                )
                if (word?.phonetic != null) {
                    Text(
                        text = "/${word.phonetic}/",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (word != null) {
                    Text(
                        text = word.pos.uppercase(),
                        fontSize = 7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

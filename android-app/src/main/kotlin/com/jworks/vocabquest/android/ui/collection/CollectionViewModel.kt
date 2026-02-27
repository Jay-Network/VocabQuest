package com.jworks.vocabquest.android.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.vocabquest.core.domain.model.CollectedWord
import com.jworks.vocabquest.core.domain.model.CollectionStats
import com.jworks.vocabquest.core.domain.model.Rarity
import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.repository.CollectionRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionUiState(
    val stats: CollectionStats = CollectionStats.EMPTY,
    val allItems: List<CollectedWord> = emptyList(),
    val filteredItems: List<CollectedWord> = emptyList(),
    val wordMap: Map<Int, Word> = emptyMap(),
    val selectedLevel: String? = null,
    val selectedRarityFilter: Rarity? = null,
    val levelCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val vocabRepository: VocabRepository
) : ViewModel() {

    companion object {
        val CEFR_LEVELS = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    }

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        loadCollection()
    }

    private fun loadCollection() {
        viewModelScope.launch {
            val stats = collectionRepository.getStats()
            val items = collectionRepository.getAll()

            // Fetch word data for all collected words
            val wordIds = items.map { it.wordId }
            val words = if (wordIds.isNotEmpty()) {
                vocabRepository.getWordsByIds(wordIds)
            } else emptyList()
            val wordMap = words.associateBy { it.id }

            // Count items per CEFR level
            val levelCounts = CEFR_LEVELS.associateWith { level ->
                items.count { item -> wordMap[item.wordId]?.cefrLevel == level }
            }

            _uiState.value = CollectionUiState(
                stats = stats,
                allItems = items,
                filteredItems = items,
                wordMap = wordMap,
                levelCounts = levelCounts,
                isLoading = false
            )
        }
    }

    fun selectLevel(level: String?) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedLevel = level,
            selectedRarityFilter = null
        )
        applyFilters()
    }

    fun filterByRarity(rarity: Rarity?) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(selectedRarityFilter = rarity)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.allItems

        // Filter by CEFR level
        if (state.selectedLevel != null) {
            filtered = filtered.filter { item ->
                state.wordMap[item.wordId]?.cefrLevel == state.selectedLevel
            }
        }

        // Filter by rarity
        if (state.selectedRarityFilter != null) {
            filtered = filtered.filter { it.rarity == state.selectedRarityFilter }
        }

        _uiState.value = state.copy(filteredItems = filtered)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadCollection()
    }
}

package com.jworks.vocabquest.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.vocabquest.core.domain.model.CoinBalance
import com.jworks.vocabquest.core.domain.model.LOCAL_USER_ID
import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.repository.JCoinRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalWords: Int = 0,
    val wordOfTheDay: Word? = null,
    val coinBalance: Long = 0,
    val currentStreak: Int = 0,
    val level: Int = 1,
    val totalXp: Int = 0,
    val levelProgress: Float = 0f,
    val wordsByLevel: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vocabRepository: VocabRepository,
    private val jCoinRepository: JCoinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeBalance()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val totalWords = vocabRepository.getTotalWordCount()
            val wordOfDay = vocabRepository.getRandomWords(1).firstOrNull()

            val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
            val wordsByLevel = levels.associateWith { vocabRepository.getWordCountByLevel(it) }

            _uiState.value = _uiState.value.copy(
                totalWords = totalWords,
                wordOfTheDay = wordOfDay,
                wordsByLevel = wordsByLevel,
                isLoading = false
            )
        }
    }

    private fun observeBalance() {
        viewModelScope.launch {
            jCoinRepository.observeBalance(LOCAL_USER_ID).collect { balance ->
                _uiState.value = _uiState.value.copy(
                    coinBalance = balance.displayBalance
                )
            }
        }
    }
}

package com.jworks.vocabquest.android.ui.game.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.vocabquest.core.domain.model.GameMode
import com.jworks.vocabquest.core.domain.model.SrsCard
import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.repository.SrsRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import com.jworks.vocabquest.core.domain.usecase.CompleteSessionUseCase
import com.jworks.vocabquest.core.engine.SessionStats
import com.jworks.vocabquest.core.scoring.ScoringEngine
import com.jworks.vocabquest.core.srs.SrsAlgorithm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

data class FlashcardUiState(
    val currentWord: Word? = null,
    val currentCard: SrsCard? = null,
    val isRevealed: Boolean = false,
    val cardsStudied: Int = 0,
    val correctCount: Int = 0,
    val totalCards: Int = 0,
    val isFinished: Boolean = false,
    val xpEarned: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val vocabRepository: VocabRepository,
    private val srsRepository: SrsRepository,
    private val srsAlgorithm: SrsAlgorithm,
    private val scoringEngine: ScoringEngine,
    private val completeSessionUseCase: CompleteSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private val studyQueue = mutableListOf<Pair<Word, SrsCard?>>()
    private var currentIndex = 0
    private var sessionStartTime = 0L
    private var totalXp = 0

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            sessionStartTime = Clock.System.now().epochSeconds
            val now = Clock.System.now().epochSeconds

            // Get cards due for review
            val dueCards = srsRepository.getCardsForReview(now, 15)
            val dueWordIds = dueCards.map { it.wordId }
            val dueWords = vocabRepository.getWordsByIds(dueWordIds)

            // Get new cards if we need more
            val remaining = 20 - dueCards.size
            val newWordIds = if (remaining > 0) srsRepository.getNewCards(remaining) else emptyList()
            val newWords = vocabRepository.getWordsByIds(newWordIds)

            // Build study queue
            studyQueue.clear()
            for (word in dueWords) {
                val card = dueCards.find { it.wordId == word.id }
                studyQueue.add(word to card)
            }
            for (word in newWords) {
                studyQueue.add(word to null)
            }

            currentIndex = 0
            showCurrentCard()
        }
    }

    private fun showCurrentCard() {
        if (currentIndex >= studyQueue.size) {
            finishSession()
            return
        }

        val (word, card) = studyQueue[currentIndex]
        _uiState.value = _uiState.value.copy(
            currentWord = word,
            currentCard = card,
            isRevealed = false,
            totalCards = studyQueue.size,
            isLoading = false
        )
    }

    fun reveal() {
        _uiState.value = _uiState.value.copy(isRevealed = true)
    }

    fun answer(quality: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            val word = state.currentWord ?: return@launch
            val now = Clock.System.now().epochSeconds
            val correct = quality >= 3

            // Update SRS card
            val existingCard = state.currentCard ?: SrsCard(wordId = word.id)
            val updatedCard = srsAlgorithm.review(existingCard, quality, now)
            srsRepository.upsertCard(updatedCard)

            // Update XP
            val xp = scoringEngine.calculateScore(if (correct) quality else 1, 0, isNewCard = state.currentCard == null).totalXp
            totalXp += xp

            _uiState.value = state.copy(
                cardsStudied = state.cardsStudied + 1,
                correctCount = state.correctCount + if (correct) 1 else 0,
                xpEarned = totalXp
            )

            currentIndex++
            showCurrentCard()
        }
    }

    private fun finishSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val duration = (Clock.System.now().epochSeconds - sessionStartTime).toInt()

            val stats = SessionStats(
                gameMode = GameMode.VOCABULARY,
                cardsStudied = state.cardsStudied,
                correctCount = state.correctCount,
                comboMax = 0,
                xpEarned = totalXp,
                durationSec = duration
            )

            try {
                completeSessionUseCase.execute(stats)
            } catch (_: Exception) {
                // Session completion is best-effort
            }

            _uiState.value = state.copy(isFinished = true)
        }
    }
}

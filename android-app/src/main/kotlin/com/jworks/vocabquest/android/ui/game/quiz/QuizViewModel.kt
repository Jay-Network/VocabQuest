package com.jworks.vocabquest.android.ui.game.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.vocabquest.core.collection.EncounterResult
import com.jworks.vocabquest.core.collection.WordEncounterEngine
import com.jworks.vocabquest.core.collection.WordLevelEngine
import com.jworks.vocabquest.core.collection.WordLevelResult
import com.jworks.vocabquest.core.domain.model.GameMode
import com.jworks.vocabquest.core.domain.usecase.CheckWordMasteryUseCase
import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.repository.SrsRepository
import com.jworks.vocabquest.core.domain.repository.SubscriptionRepository
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

data class QuizQuestion(
    val word: Word,
    val options: List<String>,
    val correctIndex: Int
)

data class QuizUiState(
    val currentQuestion: QuizQuestion? = null,
    val selectedAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 20,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val xpEarned: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val isLocked: Boolean = false,
    val lastEncounter: EncounterResult? = null,
    val lastLevelUp: WordLevelResult? = null,
    val totalDiscoveries: Int = 0
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val vocabRepository: VocabRepository,
    private val srsRepository: SrsRepository,
    private val srsAlgorithm: SrsAlgorithm,
    private val scoringEngine: ScoringEngine,
    private val completeSessionUseCase: CompleteSessionUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val encounterEngine: WordEncounterEngine,
    private val levelEngine: WordLevelEngine,
    private val checkWordMasteryUseCase: CheckWordMasteryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val questions = mutableListOf<QuizQuestion>()
    private var sessionStartTime = 0L
    private var comboStreak = 0

    init {
        checkAccessAndLoad()
    }

    private fun checkAccessAndLoad() {
        viewModelScope.launch {
            val tier = subscriptionRepository.getCurrentTier()
            if (GameMode.RECOGNITION !in tier.gameModes) {
                _uiState.value = QuizUiState(isLocked = true, isLoading = false)
                return@launch
            }
            generateQuiz()
        }
    }

    private fun generateQuiz() {
        viewModelScope.launch {
            sessionStartTime = Clock.System.now().epochSeconds

            // Get words for quiz - mix of review and new
            val now = Clock.System.now().epochSeconds
            val dueCards = srsRepository.getCardsForReview(now, 10)
            val dueWordIds = dueCards.map { it.wordId }
            val dueWords = vocabRepository.getWordsByIds(dueWordIds)

            val remaining = 20 - dueWords.size
            val newWords = vocabRepository.getRandomWords(remaining)

            val quizWords = (dueWords + newWords).take(20)

            // Need at least 4 words for multiple choice — fetch more if needed
            val allDefinitions = quizWords.map { it.definition }.distinct().toMutableList()
            if (allDefinitions.size < 4) {
                val extraWords = vocabRepository.getRandomWords(10)
                for (w in extraWords) {
                    if (w.definition !in allDefinitions) allDefinitions.add(w.definition)
                    if (allDefinitions.size >= 10) break
                }
            }

            for (word in quizWords) {
                val wrongDefs = allDefinitions
                    .filter { it != word.definition }
                    .shuffled()
                    .take(3)

                if (wrongDefs.size < 3) continue // Skip words without enough distractors

                val options = (wrongDefs + word.definition).shuffled()
                // Use indexOfFirst to find the exact correct answer reliably
                val correctAnswer = word.definition
                val correctIndex = options.indexOfFirst { it == correctAnswer }

                questions.add(QuizQuestion(word, options, correctIndex))
            }

            questions.shuffle()

            _uiState.value = QuizUiState(
                totalQuestions = questions.size,
                isLoading = false
            )

            showNextQuestion()
        }
    }

    private fun showNextQuestion() {
        val state = _uiState.value
        if (state.questionNumber >= questions.size) {
            finishQuiz()
            return
        }

        _uiState.value = state.copy(
            currentQuestion = questions[state.questionNumber],
            selectedAnswer = null,
            isCorrect = null
        )
    }

    fun selectAnswer(index: Int) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.selectedAnswer != null) return // Already answered

        val correct = index == question.correctIndex
        if (correct) comboStreak++ else comboStreak = 0

        val quality = if (correct) 4 else 1
        val xp = scoringEngine.calculateScore(quality, comboStreak, isNewCard = false).totalXp

        _uiState.value = state.copy(
            selectedAnswer = index,
            isCorrect = correct,
            correctCount = state.correctCount + if (correct) 1 else 0,
            streak = comboStreak,
            xpEarned = state.xpEarned + xp
        )

        // Update SRS for the word
        viewModelScope.launch {
            val srsQuality = if (correct) 4 else 1
            val now = Clock.System.now().epochSeconds
            val existingCard = srsRepository.getCard(question.word.id)
            val card = existingCard ?: com.jworks.vocabquest.core.domain.model.SrsCard(wordId = question.word.id)
            val updated = srsAlgorithm.review(card, srsQuality, now)
            srsRepository.upsertCard(updated)

            // Check if a received word just got mastered
            checkWordMasteryUseCase.execute(question.word.id, card.state, updated.state)

            // Collection: level up existing words + roll for new encounters
            val levelResult = levelEngine.addXp(question.word.id, correct, comboStreak)
            if (correct) {
                val encounter = encounterEngine.rollEncounter(
                    unlockedLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2"),
                    currentTime = now
                )
                _uiState.value = _uiState.value.copy(
                    lastEncounter = encounter,
                    lastLevelUp = levelResult?.takeIf { it.leveledUp },
                    totalDiscoveries = _uiState.value.totalDiscoveries + if (encounter != null) 1 else 0
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    lastEncounter = null,
                    lastLevelUp = levelResult?.takeIf { it.leveledUp }
                )
            }
        }
    }

    fun dismissEncounter() {
        _uiState.value = _uiState.value.copy(lastEncounter = null, lastLevelUp = null)
    }

    fun nextQuestion() {
        _uiState.value = _uiState.value.copy(
            questionNumber = _uiState.value.questionNumber + 1
        )
        showNextQuestion()
    }

    private fun finishQuiz() {
        viewModelScope.launch {
            val state = _uiState.value
            val duration = (Clock.System.now().epochSeconds - sessionStartTime).toInt()

            val stats = SessionStats(
                gameMode = GameMode.RECOGNITION,
                cardsStudied = state.totalQuestions,
                correctCount = state.correctCount,
                comboMax = state.streak,
                xpEarned = state.xpEarned,
                durationSec = duration
            )

            try {
                completeSessionUseCase.execute(stats)
            } catch (e: Exception) {
                android.util.Log.e("QuizViewModel", "Failed to save session: ${e.message}", e)
            }

            _uiState.value = state.copy(isFinished = true)
        }
    }
}

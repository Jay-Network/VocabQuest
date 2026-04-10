package com.jworks.eigojourney.android.ui.worddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.eigojourney.core.domain.model.SrsCard
import com.jworks.eigojourney.core.domain.model.SrsState
import com.jworks.eigojourney.core.domain.model.Word
import com.jworks.eigojourney.core.domain.repository.SrsRepository
import com.jworks.eigojourney.core.domain.repository.VocabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordDetailUiState(
    val word: Word? = null,
    val srsCard: SrsCard? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
) {
    val srsStateLabel: String get() = when (srsCard?.state) {
        SrsState.NEW -> "New"
        SrsState.LEARNING -> "Learning"
        SrsState.REVIEW -> "Review"
        SrsState.GRADUATED -> "Mastered"
        null -> "Not started"
    }

    val accuracyPercent: Int get() = ((srsCard?.accuracy ?: 0f) * 100).toInt()

    val nextReviewLabel: String get() {
        val nextReview = srsCard?.nextReview ?: return "Not scheduled"
        if (nextReview == 0L) return "Not scheduled"
        val now = System.currentTimeMillis() / 1000
        val diff = nextReview - now
        return when {
            diff <= 0 -> "Due now"
            diff < 3600 -> "${diff / 60} min"
            diff < 86400 -> "${diff / 3600} hours"
            else -> "${diff / 86400} days"
        }
    }
}

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vocabRepository: VocabRepository,
    private val srsRepository: SrsRepository
) : ViewModel() {

    private val wordId: Int = savedStateHandle.get<Int>("wordId") ?: 0

    private val _uiState = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    init {
        loadWord()
    }

    private fun loadWord() {
        viewModelScope.launch {
            val word = vocabRepository.getWordById(wordId)
            if (word == null) {
                _uiState.value = WordDetailUiState(isLoading = false, notFound = true)
                return@launch
            }
            val srsCard = srsRepository.getCard(wordId)
            _uiState.value = WordDetailUiState(
                word = word,
                srsCard = srsCard,
                isLoading = false
            )
        }
    }
}

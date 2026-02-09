package com.jworks.vocabquest.android.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.vocabquest.core.domain.model.DailyStatsData
import com.jworks.vocabquest.core.domain.model.StudySession
import com.jworks.vocabquest.core.domain.model.UserProfile
import com.jworks.vocabquest.core.domain.repository.SessionRepository
import com.jworks.vocabquest.core.domain.repository.SrsRepository
import com.jworks.vocabquest.core.domain.repository.UserRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class ProgressUiState(
    val profile: UserProfile = UserProfile(),
    val totalWords: Int = 0,
    val wordsReviewed: Int = 0,
    val wordsMastered: Int = 0,
    val recentSessions: List<StudySession> = emptyList(),
    val weeklyStats: List<DailyStatsData> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val srsRepository: SrsRepository,
    private val vocabRepository: VocabRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val profile = userRepository.getProfile()
            val totalWords = vocabRepository.getTotalWordCount()
            val wordsReviewed = srsRepository.getTotalReviewedCount()
            val wordsMastered = srsRepository.getMasteredCount()
            val recentSessions = sessionRepository.getRecentSessions(10)

            // Get last 7 days of stats
            val now = Clock.System.now()
            val tz = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(tz).date
            val weekAgo = Instant.fromEpochSeconds(now.epochSeconds - 7 * 86400)
                .toLocalDateTime(tz).date
            val weeklyStats = sessionRepository.getDailyStatsRange(
                weekAgo.toString(),
                today.toString()
            )

            _uiState.value = ProgressUiState(
                profile = profile,
                totalWords = totalWords,
                wordsReviewed = wordsReviewed,
                wordsMastered = wordsMastered,
                recentSessions = recentSessions,
                weeklyStats = weeklyStats,
                isLoading = false
            )
        }
    }
}

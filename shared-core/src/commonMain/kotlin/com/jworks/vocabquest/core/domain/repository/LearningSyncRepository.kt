package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.DailyStatsData
import com.jworks.vocabquest.core.domain.model.StudySession
import com.jworks.vocabquest.core.domain.model.UserProfile

interface LearningSyncRepository {
    suspend fun queueSessionSync(
        userId: String,
        touchedKanjiIds: List<Int>,
        touchedVocabIds: List<Long>,
        profile: UserProfile,
        session: StudySession,
        dailyStats: DailyStatsData,
        achievements: List<Any>
    )
}

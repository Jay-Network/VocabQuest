package com.jworks.vocabquest.core.domain.model

data class DailyStatsData(
    val date: String,
    val cardsReviewed: Int,
    val xpEarned: Int,
    val studyTimeSec: Int
)

package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.SrsCard

interface SrsRepository {
    suspend fun getCard(wordId: Int): SrsCard?
    suspend fun getCardsForReview(currentTime: Long, limit: Int): List<SrsCard>
    suspend fun getNewCards(limit: Int): List<Int>
    suspend fun upsertCard(card: SrsCard)
    suspend fun getTotalReviewedCount(): Int
    suspend fun getMasteredCount(): Int
}

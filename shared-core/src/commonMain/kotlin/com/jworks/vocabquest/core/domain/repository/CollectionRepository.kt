package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.CollectedWord
import com.jworks.vocabquest.core.domain.model.CollectionStats
import com.jworks.vocabquest.core.domain.model.Rarity

interface CollectionRepository {
    suspend fun getAll(): List<CollectedWord>
    suspend fun getByRarity(rarity: Rarity): List<CollectedWord>
    suspend fun getItem(wordId: Int): CollectedWord?
    suspend fun isCollected(wordId: Int): Boolean
    suspend fun collect(item: CollectedWord)
    suspend fun addItemXp(wordId: Int, xp: Int): CollectedWord?
    suspend fun updateLevel(wordId: Int, newLevel: Int, overflowXp: Int)
    suspend fun getCollectedWordIds(): List<Int>
    suspend fun getTotalCount(): Long
    suspend fun getCountByRarity(rarity: Rarity): Long
    suspend fun getStats(): CollectionStats
    suspend fun getRecent(limit: Int = 10): List<CollectedWord>
}

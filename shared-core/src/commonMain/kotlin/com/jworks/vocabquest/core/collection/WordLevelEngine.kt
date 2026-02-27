package com.jworks.vocabquest.core.collection

import com.jworks.vocabquest.core.domain.model.CollectedWord
import com.jworks.vocabquest.core.domain.repository.CollectionRepository

data class WordLevelResult(
    val updatedWord: CollectedWord,
    val leveledUp: Boolean,
    val xpGained: Int
)

class WordLevelEngine(
    private val collectionRepository: CollectionRepository
) {
    companion object {
        private const val XP_CORRECT = 10
        private const val XP_WRONG = 2
        private const val MAX_LEVEL = 10

        fun xpForLevel(level: Int): Int = level * level * 25
    }

    /**
     * Add XP to a collected word after it appears in a game session.
     * @param wordId The word ID
     * @param isCorrect Whether the player answered correctly
     * @param comboCount Current combo count for bonus XP
     * @return WordLevelResult with updated word and whether it leveled up, or null if not collected
     */
    suspend fun addXp(
        wordId: Int,
        isCorrect: Boolean,
        comboCount: Int = 0
    ): WordLevelResult? {
        if (!collectionRepository.isCollected(wordId)) return null

        val baseXp = if (isCorrect) XP_CORRECT else XP_WRONG
        val comboBonus = if (isCorrect && comboCount >= 5) baseXp / 2 else 0
        val totalXp = baseXp + comboBonus

        val updated = collectionRepository.addItemXp(wordId, totalXp)
            ?: return null

        // Check for level up
        if (!updated.isMaxLevel && updated.itemXp >= updated.xpToNextLevel) {
            val newLevel = (updated.itemLevel + 1).coerceAtMost(MAX_LEVEL)
            val overflowXp = updated.itemXp - updated.xpToNextLevel
            collectionRepository.updateLevel(wordId, newLevel, overflowXp.coerceAtLeast(0))
            val finalWord = collectionRepository.getItem(wordId) ?: updated
            return WordLevelResult(
                updatedWord = finalWord,
                leveledUp = true,
                xpGained = totalXp
            )
        }

        return WordLevelResult(
            updatedWord = updated,
            leveledUp = false,
            xpGained = totalXp
        )
    }
}

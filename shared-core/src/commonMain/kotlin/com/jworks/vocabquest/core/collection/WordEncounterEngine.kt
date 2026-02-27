package com.jworks.vocabquest.core.collection

import com.jworks.vocabquest.core.domain.model.CollectedWord
import com.jworks.vocabquest.core.domain.model.EarnTriggers
import com.jworks.vocabquest.core.domain.model.LOCAL_USER_ID
import com.jworks.vocabquest.core.domain.model.Rarity
import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.repository.CollectionRepository
import com.jworks.vocabquest.core.domain.repository.JCoinRepository
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import kotlin.random.Random

data class EncounterResult(
    val collectedWord: CollectedWord,
    val word: Word?
)

class WordEncounterEngine(
    private val collectionRepository: CollectionRepository,
    private val vocabRepository: VocabRepository,
    private val jCoinRepository: JCoinRepository? = null
) {
    // Pity counters: correct answers since last discovery per rarity
    private var pityCounts = mutableMapOf(
        Rarity.COMMON to 0,
        Rarity.UNCOMMON to 0,
        Rarity.RARE to 0,
        Rarity.EPIC to 0,
        Rarity.LEGENDARY to 0
    )

    companion object {
        // Base encounter rates per correct answer
        private val ENCOUNTER_RATES = mapOf(
            Rarity.COMMON to 0.40f,
            Rarity.UNCOMMON to 0.25f,
            Rarity.RARE to 0.12f,
            Rarity.EPIC to 0.05f,
            Rarity.LEGENDARY to 0.02f
        )

        // Pity thresholds: guaranteed encounter after N correct answers without discovery
        private val PITY_THRESHOLDS = mapOf(
            Rarity.COMMON to 3,
            Rarity.UNCOMMON to 5,
            Rarity.RARE to 12,
            Rarity.EPIC to 25,
            Rarity.LEGENDARY to 50
        )
    }

    /**
     * Roll for an encounter after a correct answer.
     * @param unlockedLevels The CEFR levels the player has access to
     * @param currentTime Epoch seconds for discovered_at
     * @return EncounterResult if a new word was discovered, null otherwise
     */
    suspend fun rollEncounter(
        unlockedLevels: List<String>,
        currentTime: Long
    ): EncounterResult? {
        // Increment all pity counters
        for (rarity in Rarity.entries) {
            pityCounts[rarity] = (pityCounts[rarity] ?: 0) + 1
        }

        // Determine which rarity to try (highest first for excitement)
        val targetRarity = determineEncounterRarity()
            ?: return null

        // Find an uncollected word of this rarity from unlocked levels
        val item = findUncollectedWord(targetRarity, unlockedLevels, currentTime)
            ?: return null

        // Reset pity counter for the discovered rarity (and all lower rarities)
        resetPityCounters(targetRarity)

        // Add to collection
        collectionRepository.collect(item)

        // Award J Coins for discovery
        awardCollectionCoins(item)

        // Fetch the full word data for display
        val word = vocabRepository.getWordById(item.wordId)

        return EncounterResult(item, word)
    }

    /**
     * Determine which rarity tier triggers based on probability + pity.
     */
    private fun determineEncounterRarity(): Rarity? {
        // Check from highest to lowest rarity
        for (rarity in Rarity.entries.reversed()) {
            val pityCount = pityCounts[rarity] ?: 0
            val pityThreshold = PITY_THRESHOLDS[rarity] ?: Int.MAX_VALUE
            val baseRate = ENCOUNTER_RATES[rarity] ?: 0f

            // Pity guarantee
            if (pityCount >= pityThreshold) {
                return rarity
            }

            // Probability roll
            if (Random.nextFloat() < baseRate) {
                return rarity
            }
        }
        return null
    }

    private fun resetPityCounters(discoveredRarity: Rarity) {
        for (rarity in Rarity.entries) {
            if (rarity.ordinal <= discoveredRarity.ordinal) {
                pityCounts[rarity] = 0
            }
        }
    }

    /**
     * Find a random uncollected word of the target rarity from unlocked CEFR levels.
     */
    private suspend fun findUncollectedWord(
        targetRarity: Rarity,
        unlockedLevels: List<String>,
        currentTime: Long
    ): CollectedWord? {
        val collectedIds = collectionRepository.getCollectedWordIds().toSet()

        // Try each unlocked level in random order
        for (level in unlockedLevels.shuffled()) {
            val levelWords = vocabRepository.getWordsByLevel(level, limit = 500)
            val candidates = levelWords.filter { word ->
                word.id !in collectedIds &&
                    WordRarityCalculator.calculateRarity(word.cefrLevel, word.frequencyRank) == targetRarity
            }

            if (candidates.isNotEmpty()) {
                val chosen = candidates.random()
                return CollectedWord(
                    wordId = chosen.id,
                    rarity = targetRarity,
                    itemLevel = 1,
                    itemXp = 0,
                    discoveredAt = currentTime,
                    source = "gameplay"
                )
            }
        }

        // Fallback: try one tier lower
        val fallbackRarity = if (targetRarity.ordinal > 0) {
            Rarity.entries[targetRarity.ordinal - 1]
        } else null

        if (fallbackRarity != null) {
            for (level in unlockedLevels.shuffled()) {
                val levelWords = vocabRepository.getWordsByLevel(level, limit = 500)
                val candidates = levelWords.filter { word ->
                    word.id !in collectedIds &&
                        WordRarityCalculator.calculateRarity(word.cefrLevel, word.frequencyRank) == fallbackRarity
                }
                if (candidates.isNotEmpty()) {
                    val chosen = candidates.random()
                    return CollectedWord(
                        wordId = chosen.id,
                        rarity = fallbackRarity,
                        itemLevel = 1,
                        itemXp = 0,
                        discoveredAt = currentTime,
                        source = "gameplay"
                    )
                }
            }
        }

        // Last resort: any uncollected word from unlocked levels
        for (level in unlockedLevels.shuffled()) {
            val levelWords = vocabRepository.getWordsByLevel(level, limit = 500)
            val candidates = levelWords.filter { it.id !in collectedIds }
            if (candidates.isNotEmpty()) {
                val chosen = candidates.random()
                val rarity = WordRarityCalculator.calculateRarity(chosen.cefrLevel, chosen.frequencyRank)
                return CollectedWord(
                    wordId = chosen.id,
                    rarity = rarity,
                    itemLevel = 1,
                    itemXp = 0,
                    discoveredAt = currentTime,
                    source = "gameplay"
                )
            }
        }

        return null
    }

    private suspend fun awardCollectionCoins(item: CollectedWord) {
        val repo = jCoinRepository ?: return

        // WORD_COLLECTED: 2 coins for any discovery
        repo.earnCoins(LOCAL_USER_ID, EarnTriggers.WORD_COLLECTED, 2,
            "Discovered a new word!")

        // RARE_COLLECTED: 10 coins for Rare or higher
        if (item.rarity.ordinal >= Rarity.RARE.ordinal) {
            repo.earnCoins(LOCAL_USER_ID, EarnTriggers.RARE_COLLECTED, 10,
                "Discovered a ${item.rarity.label} word!")
        }

        // COLLECTION_50: 75 coins milestone
        val stats = collectionRepository.getStats()
        if (stats.totalCollected == 50) {
            repo.earnCoins(LOCAL_USER_ID, EarnTriggers.COLLECTION_50, 75,
                "Collected 50 words!")
        }
    }

    fun resetPity() {
        pityCounts.keys.forEach { key -> pityCounts[key] = 0 }
    }
}

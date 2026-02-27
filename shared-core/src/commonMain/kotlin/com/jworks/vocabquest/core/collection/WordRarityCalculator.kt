package com.jworks.vocabquest.core.collection

import com.jworks.vocabquest.core.domain.model.Rarity

object WordRarityCalculator {

    /**
     * Calculate rarity for an English word based on CEFR level and frequency rank.
     *
     * Rarity distribution across 10,000 words:
     * - COMMON (~40%): High-frequency everyday words (A1, top 2000)
     * - UNCOMMON (~25%): Intermediate words (A2/B1, rank 2001-5000)
     * - RARE (~15%): Upper-intermediate words (B2, rank 5001-7000)
     * - EPIC (~12%): Advanced words (C1, rank 7001-9000)
     * - LEGENDARY (~8%): Expert-level words (C2, rank 9000+)
     */
    fun calculateRarity(cefrLevel: String, frequencyRank: Int): Rarity {
        return when {
            // C2 words are always legendary
            cefrLevel == "C2" -> Rarity.LEGENDARY
            // C1 words are epic, unless very common
            cefrLevel == "C1" && frequencyRank > 5000 -> Rarity.LEGENDARY
            cefrLevel == "C1" -> Rarity.EPIC
            // B2 words are rare, higher frequency ones are uncommon
            cefrLevel == "B2" && frequencyRank > 7000 -> Rarity.EPIC
            cefrLevel == "B2" -> Rarity.RARE
            // B1 words span uncommon to rare
            cefrLevel == "B1" && frequencyRank > 5000 -> Rarity.RARE
            cefrLevel == "B1" -> Rarity.UNCOMMON
            // A2 words are generally uncommon
            cefrLevel == "A2" && frequencyRank > 3000 -> Rarity.UNCOMMON
            cefrLevel == "A2" -> Rarity.COMMON
            // A1 words are always common
            cefrLevel == "A1" -> Rarity.COMMON
            // Fallback: use frequency rank alone
            frequencyRank > 9000 -> Rarity.LEGENDARY
            frequencyRank > 7000 -> Rarity.EPIC
            frequencyRank > 5000 -> Rarity.RARE
            frequencyRank > 2000 -> Rarity.UNCOMMON
            else -> Rarity.COMMON
        }
    }
}

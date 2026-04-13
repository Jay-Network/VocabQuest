package com.jworks.eigojourney.core.collection

import com.jworks.eigojourney.core.domain.model.Rarity
import kotlin.test.Test
import kotlin.test.assertEquals

class WordRarityCalculatorTest {

    @Test
    fun `C2 is always legendary regardless of frequency`() {
        assertEquals(Rarity.LEGENDARY, WordRarityCalculator.calculateRarity("C2", 100))
        assertEquals(Rarity.LEGENDARY, WordRarityCalculator.calculateRarity("C2", 9999))
    }

    @Test
    fun `C1 rare frequency becomes legendary`() {
        assertEquals(Rarity.LEGENDARY, WordRarityCalculator.calculateRarity("C1", 9000))
    }

    @Test
    fun `C1 common frequency is epic`() {
        assertEquals(Rarity.EPIC, WordRarityCalculator.calculateRarity("C1", 4000))
    }

    @Test
    fun `B2 high rank becomes epic`() {
        assertEquals(Rarity.EPIC, WordRarityCalculator.calculateRarity("B2", 8000))
    }

    @Test
    fun `B2 typical is rare`() {
        assertEquals(Rarity.RARE, WordRarityCalculator.calculateRarity("B2", 6000))
    }

    @Test
    fun `B1 high rank is rare`() {
        assertEquals(Rarity.RARE, WordRarityCalculator.calculateRarity("B1", 5500))
    }

    @Test
    fun `B1 typical is uncommon`() {
        assertEquals(Rarity.UNCOMMON, WordRarityCalculator.calculateRarity("B1", 3000))
    }

    @Test
    fun `A2 high rank is uncommon`() {
        assertEquals(Rarity.UNCOMMON, WordRarityCalculator.calculateRarity("A2", 4000))
    }

    @Test
    fun `A2 low rank is common`() {
        assertEquals(Rarity.COMMON, WordRarityCalculator.calculateRarity("A2", 1500))
    }

    @Test
    fun `A1 is always common`() {
        assertEquals(Rarity.COMMON, WordRarityCalculator.calculateRarity("A1", 50))
        assertEquals(Rarity.COMMON, WordRarityCalculator.calculateRarity("A1", 2500))
    }

    @Test
    fun `unknown CEFR falls back to frequency-only classification`() {
        assertEquals(Rarity.LEGENDARY, WordRarityCalculator.calculateRarity("", 9500))
        assertEquals(Rarity.EPIC, WordRarityCalculator.calculateRarity("", 8000))
        assertEquals(Rarity.RARE, WordRarityCalculator.calculateRarity("", 6000))
        assertEquals(Rarity.UNCOMMON, WordRarityCalculator.calculateRarity("?", 3000))
        assertEquals(Rarity.COMMON, WordRarityCalculator.calculateRarity("?", 500))
    }
}

package com.jworks.eigojourney.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RarityTest {

    @Test
    fun `fromString matches case-insensitively`() {
        assertEquals(Rarity.LEGENDARY, Rarity.fromString("legendary"))
        assertEquals(Rarity.LEGENDARY, Rarity.fromString("LEGENDARY"))
        assertEquals(Rarity.EPIC, Rarity.fromString("Epic"))
    }

    @Test
    fun `fromString defaults to COMMON for unknown`() {
        assertEquals(Rarity.COMMON, Rarity.fromString("mythic"))
        assertEquals(Rarity.COMMON, Rarity.fromString(""))
    }

    @Test
    fun `ordinal order matches rarity progression`() {
        assertTrue(Rarity.COMMON.ordinal < Rarity.UNCOMMON.ordinal)
        assertTrue(Rarity.UNCOMMON.ordinal < Rarity.RARE.ordinal)
        assertTrue(Rarity.RARE.ordinal < Rarity.EPIC.ordinal)
        assertTrue(Rarity.EPIC.ordinal < Rarity.LEGENDARY.ordinal)
    }
}

class SrsStateTest {

    @Test
    fun `fromString round-trips all states`() {
        for (state in SrsState.entries) {
            assertEquals(state, SrsState.fromString(state.value))
        }
    }

    @Test
    fun `fromString defaults to NEW for unknown`() {
        assertEquals(SrsState.NEW, SrsState.fromString("unknown"))
        assertEquals(SrsState.NEW, SrsState.fromString(""))
    }
}

class GameModeTest {

    @Test
    fun `fromString round-trips all modes`() {
        for (mode in GameMode.entries) {
            assertEquals(mode, GameMode.fromString(mode.value))
        }
    }

    @Test
    fun `fromString defaults to RECOGNITION for unknown`() {
        assertEquals(GameMode.RECOGNITION, GameMode.fromString("unknown"))
    }
}

class CollectedWordTest {

    private fun word(level: Int = 1, xp: Int = 0) = CollectedWord(
        wordId = 1, rarity = Rarity.RARE, itemLevel = level,
        itemXp = xp, discoveredAt = 0L, source = "test"
    )

    @Test
    fun `xpToNextLevel scales quadratically`() {
        assertEquals(25, word(level = 1).xpToNextLevel)   // 1*1*25
        assertEquals(100, word(level = 2).xpToNextLevel)  // 2*2*25
        assertEquals(225, word(level = 3).xpToNextLevel)  // 3*3*25
    }

    @Test
    fun `levelProgress is fraction of xp to next`() {
        assertEquals(0.5f, word(level = 2, xp = 50).levelProgress) // 50/100
        assertEquals(0f, word(level = 1, xp = 0).levelProgress)
    }

    @Test
    fun `isMaxLevel at level 10`() {
        assertFalse(word(level = 9).isMaxLevel)
        assertTrue(word(level = 10).isMaxLevel)
        assertTrue(word(level = 11).isMaxLevel)
    }

    @Test
    fun `levelProgress is 1 at max level`() {
        assertEquals(1f, word(level = 10).levelProgress)
    }

    @Test
    fun `accuracy is zero with no reviews`() {
        val card = SrsCard(wordId = 1)
        assertEquals(0f, card.accuracy)
    }

    @Test
    fun `accuracy calculates correctly`() {
        val card = SrsCard(wordId = 1, totalReviews = 10, correctCount = 7)
        assertEquals(0.7f, card.accuracy)
    }
}

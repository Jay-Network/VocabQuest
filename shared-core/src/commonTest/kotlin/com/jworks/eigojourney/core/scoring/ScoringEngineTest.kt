package com.jworks.eigojourney.core.scoring

import com.jworks.eigojourney.core.domain.model.GameMode
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringEngineTest {

    private val engine = ScoringEngine()

    @Test
    fun `recognition quality 3 gives 8 base xp`() {
        val result = engine.calculateScore(quality = 3, comboCount = 0, isNewCard = false)
        assertEquals(8, result.baseXp)
        assertEquals(1.0f, result.comboMultiplier)
        assertEquals(8, result.totalXp)
    }

    @Test
    fun `recognition quality 5 gives 15 base xp`() {
        val result = engine.calculateScore(quality = 5, comboCount = 0, isNewCard = false)
        assertEquals(15, result.baseXp)
        assertEquals(15, result.totalXp)
    }

    @Test
    fun `writing mode pays more than recognition at same quality`() {
        val rec = engine.calculateScore(5, 0, false, GameMode.RECOGNITION)
        val writ = engine.calculateScore(5, 0, false, GameMode.WRITING)
        assertEquals(15, rec.baseXp)
        assertEquals(20, writ.baseXp)
    }

    @Test
    fun `quality below 3 gives zero xp`() {
        val result = engine.calculateScore(quality = 2, comboCount = 5, isNewCard = true)
        assertEquals(0, result.baseXp)
        assertEquals(0, result.totalXp)
    }

    @Test
    fun `combo 3 to 4 applies 1_2 multiplier`() {
        val result = engine.calculateScore(quality = 5, comboCount = 3, isNewCard = false)
        assertEquals(1.2f, result.comboMultiplier)
        assertEquals(18, result.totalXp) // 15 * 1.2
    }

    @Test
    fun `combo 5 to 9 applies 1_5 multiplier`() {
        val result = engine.calculateScore(quality = 5, comboCount = 5, isNewCard = false)
        assertEquals(1.5f, result.comboMultiplier)
        assertEquals(22, result.totalXp) // 15 * 1.5 = 22.5 -> 22
    }

    @Test
    fun `combo 10 plus applies 2x multiplier`() {
        val result = engine.calculateScore(quality = 5, comboCount = 10, isNewCard = false)
        assertEquals(2.0f, result.comboMultiplier)
        assertEquals(30, result.totalXp)
    }

    @Test
    fun `new card adds 1_5x on top of combo`() {
        val result = engine.calculateScore(quality = 5, comboCount = 10, isNewCard = true)
        // 15 * 2.0 * 1.5 = 45
        assertEquals(45, result.totalXp)
        assertEquals(true, result.isNewCardBonus)
    }

    @Test
    fun `level 1 requires zero xp`() {
        assertEquals(1, engine.calculateLevel(0))
        assertEquals(1, engine.calculateLevel(199))
    }

    @Test
    fun `level 2 at 200 xp`() {
        // xpForLevel(2) = 2*2*50 = 200
        assertEquals(2, engine.calculateLevel(200))
        assertEquals(2, engine.calculateLevel(449))
    }

    @Test
    fun `level 3 at 450 xp`() {
        assertEquals(3, engine.calculateLevel(450))
    }

    @Test
    fun `level scales quadratically`() {
        assertEquals(5, engine.calculateLevel(1250))  // 5*5*50 = 1250
        assertEquals(10, engine.calculateLevel(5000)) // 10*10*50 = 5000
    }

    @Test
    fun `xpForLevel matches square formula`() {
        assertEquals(50, ScoringEngine.xpForLevel(1))
        assertEquals(200, ScoringEngine.xpForLevel(2))
        assertEquals(1250, ScoringEngine.xpForLevel(5))
    }
}

package com.jworks.eigojourney.core.srs

import com.jworks.eigojourney.core.domain.model.SrsCard
import com.jworks.eigojourney.core.domain.model.SrsState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class Sm2AlgorithmTest {

    private val algorithm = Sm2Algorithm()
    private val now = 1_700_000_000L
    private val secondsPerDay = 86_400L
    private val learningStep = 600L

    private fun newCard() = SrsCard(wordId = 1)

    @Test
    fun `quality must be in 0 to 5`() {
        assertFailsWith<IllegalArgumentException> { algorithm.review(newCard(), -1, now) }
        assertFailsWith<IllegalArgumentException> { algorithm.review(newCard(), 6, now) }
    }

    @Test
    fun `fail resets repetitions and schedules short retry`() {
        val card = newCard().copy(repetitions = 3, interval = 10, state = SrsState.REVIEW)
        val result = algorithm.review(card, quality = 2, currentTime = now)

        assertEquals(0, result.repetitions)
        assertEquals(0, result.interval)
        assertEquals(SrsState.LEARNING, result.state)
        assertEquals(now + learningStep, result.nextReview)
        assertEquals(1, result.totalReviews)
        assertEquals(0, result.correctCount)
    }

    @Test
    fun `fail preserves prior correct count`() {
        val card = newCard().copy(totalReviews = 5, correctCount = 4)
        val result = algorithm.review(card, quality = 0, currentTime = now)

        assertEquals(6, result.totalReviews)
        assertEquals(4, result.correctCount)
    }

    @Test
    fun `first pass schedules 1 day interval and increments correct count`() {
        val result = algorithm.review(newCard(), quality = 4, currentTime = now)

        assertEquals(1, result.repetitions)
        assertEquals(0, result.interval) // still learning after 1 rep
        assertEquals(SrsState.LEARNING, result.state)
        assertEquals(now + learningStep, result.nextReview)
        assertEquals(1, result.correctCount)
    }

    @Test
    fun `second pass graduates to review state with 6 day interval`() {
        val card = newCard().copy(repetitions = 1, state = SrsState.LEARNING)
        val result = algorithm.review(card, quality = 4, currentTime = now)

        assertEquals(2, result.repetitions)
        assertEquals(6, result.interval)
        assertEquals(SrsState.REVIEW, result.state)
        assertEquals(now + 6 * secondsPerDay, result.nextReview)
    }

    @Test
    fun `subsequent passes multiply by ease factor`() {
        val card = newCard().copy(repetitions = 2, interval = 6, easeFactor = 2.5, state = SrsState.REVIEW)
        val result = algorithm.review(card, quality = 4, currentTime = now)

        // interval * easeFactor with quality=4 keeps ease factor stable
        assertEquals(15, result.interval) // (6 * 2.5).toInt()
        assertEquals(SrsState.REVIEW, result.state)
    }

    @Test
    fun `ease factor has floor of 1 point 3`() {
        // Repeated quality=3 reviews decay ease factor; ensure it never drops below MIN_EASE_FACTOR
        var card = newCard().copy(easeFactor = 1.35, repetitions = 2, interval = 5, state = SrsState.REVIEW)
        repeat(10) {
            card = algorithm.review(card, quality = 3, currentTime = now)
        }
        assertTrue(card.easeFactor >= 1.3, "Ease factor dropped below floor: ${card.easeFactor}")
    }

    @Test
    fun `reaches graduated state after threshold reps`() {
        var card = newCard()
        repeat(8) {
            card = algorithm.review(card, quality = 5, currentTime = now)
        }
        assertEquals(SrsState.GRADUATED, card.state)
        assertEquals(8, card.repetitions)
    }
}

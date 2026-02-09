package com.jworks.vocabquest.core.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import com.jworks.vocabquest.core.domain.model.SrsCard
import com.jworks.vocabquest.core.domain.model.SrsState
import com.jworks.vocabquest.core.domain.repository.SrsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SrsRepositoryImpl(
    private val driver: SqlDriver
) : SrsRepository {

    override suspend fun getCard(wordId: Int): SrsCard? = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT word_id, ease_factor, interval, repetitions, next_review, state, total_reviews, correct_count FROM srs_card WHERE word_id = ?",
            mapper = { cursor ->
                val result = if (cursor.next().value) cursor.toSrsCard() else null
                QueryResult.Value(result)
            },
            parameters = 1
        ) {
            bindLong(0, wordId.toLong())
        }.value
    }

    override suspend fun getCardsForReview(currentTime: Long, limit: Int): List<SrsCard> =
        withContext(Dispatchers.Default) {
            driver.executeQuery(
                identifier = null,
                sql = "SELECT word_id, ease_factor, interval, repetitions, next_review, state, total_reviews, correct_count FROM srs_card WHERE next_review <= ? AND state != 'graduated' ORDER BY next_review ASC LIMIT ?",
                mapper = { cursor ->
                    val cards = mutableListOf<SrsCard>()
                    while (cursor.next().value) {
                        cards.add(cursor.toSrsCard())
                    }
                    QueryResult.Value(cards.toList())
                },
                parameters = 2
            ) {
                bindLong(0, currentTime)
                bindLong(1, limit.toLong())
            }.value
        }

    override suspend fun getNewCards(limit: Int): List<Int> = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id FROM word WHERE id NOT IN (SELECT word_id FROM srs_card) ORDER BY frequency_rank LIMIT ?",
            mapper = { cursor ->
                val ids = mutableListOf<Int>()
                while (cursor.next().value) {
                    ids.add(cursor.getLong(0)!!.toInt())
                }
                QueryResult.Value(ids.toList())
            },
            parameters = 1
        ) {
            bindLong(0, limit.toLong())
        }.value
    }

    override suspend fun upsertCard(card: SrsCard) = withContext(Dispatchers.Default) {
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO srs_card(word_id, ease_factor, interval, repetitions, next_review, state, total_reviews, correct_count)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(word_id) DO UPDATE SET
                    ease_factor = excluded.ease_factor,
                    interval = excluded.interval,
                    repetitions = excluded.repetitions,
                    next_review = excluded.next_review,
                    state = excluded.state,
                    total_reviews = excluded.total_reviews,
                    correct_count = excluded.correct_count
            """.trimIndent(),
            parameters = 8
        ) {
            bindLong(0, card.wordId.toLong())
            bindDouble(1, card.easeFactor)
            bindLong(2, card.interval.toLong())
            bindLong(3, card.repetitions.toLong())
            bindLong(4, card.nextReview)
            bindString(5, card.state.value)
            bindLong(6, card.totalReviews.toLong())
            bindLong(7, card.correctCount.toLong())
        }
        Unit
    }

    override suspend fun getTotalReviewedCount(): Int = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM srs_card WHERE total_reviews > 0",
            mapper = { cursor ->
                cursor.next()
                QueryResult.Value(cursor.getLong(0)!!.toInt())
            },
            parameters = 0
        ).value
    }

    override suspend fun getMasteredCount(): Int = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM srs_card WHERE state = 'graduated'",
            mapper = { cursor ->
                cursor.next()
                QueryResult.Value(cursor.getLong(0)!!.toInt())
            },
            parameters = 0
        ).value
    }
}

private fun SqlCursor.toSrsCard() = SrsCard(
    wordId = getLong(0)!!.toInt(),
    easeFactor = getDouble(1)!!,
    interval = getLong(2)!!.toInt(),
    repetitions = getLong(3)!!.toInt(),
    nextReview = getLong(4)!!,
    state = SrsState.fromString(getString(5) ?: "new"),
    totalReviews = getLong(6)!!.toInt(),
    correctCount = getLong(7)!!.toInt()
)

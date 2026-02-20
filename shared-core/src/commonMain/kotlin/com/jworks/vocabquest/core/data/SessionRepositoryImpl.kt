package com.jworks.vocabquest.core.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.jworks.vocabquest.core.domain.model.DailyStatsData
import com.jworks.vocabquest.core.domain.model.StudySession
import com.jworks.vocabquest.core.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val driver: SqlDriver
) : SessionRepository {

    override suspend fun recordSession(session: StudySession): Long = withContext(Dispatchers.Default) {
        driver.execute(
            identifier = null,
            sql = "INSERT INTO study_session(game_mode, started_at, cards_studied, correct_count, xp_earned, duration_sec) VALUES (?, ?, ?, ?, ?, ?)",
            parameters = 6
        ) {
            bindString(0, session.gameMode)
            bindLong(1, session.startedAt)
            bindLong(2, session.cardsStudied.toLong())
            bindLong(3, session.correctCount.toLong())
            bindLong(4, session.xpEarned.toLong())
            bindLong(5, session.durationSec.toLong())
        }

        driver.executeQuery(
            identifier = null,
            sql = "SELECT last_insert_rowid()",
            mapper = { cursor ->
                if (cursor.next().value) {
                    QueryResult.Value(cursor.getLong(0) ?: 0L)
                } else {
                    QueryResult.Value(0L)
                }
            },
            parameters = 0
        ).value
    }

    override suspend fun getRecentSessions(limit: Int): List<StudySession> = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, game_mode, started_at, cards_studied, correct_count, xp_earned, duration_sec FROM study_session ORDER BY started_at DESC LIMIT ?",
            mapper = { cursor ->
                val sessions = mutableListOf<StudySession>()
                while (cursor.next().value) {
                    sessions.add(
                        StudySession(
                            id = cursor.getLong(0) ?: 0L,
                            gameMode = cursor.getString(1) ?: "unknown",
                            startedAt = cursor.getLong(2) ?: 0L,
                            cardsStudied = (cursor.getLong(3) ?: 0).toInt(),
                            correctCount = (cursor.getLong(4) ?: 0).toInt(),
                            xpEarned = (cursor.getLong(5) ?: 0).toInt(),
                            durationSec = (cursor.getLong(6) ?: 0).toInt()
                        )
                    )
                }
                QueryResult.Value(sessions.toList())
            },
            parameters = 1
        ) {
            bindLong(0, limit.toLong())
        }.value
    }

    override suspend fun recordDailyStats(
        date: String,
        cardsReviewed: Int,
        xpEarned: Int,
        studyTimeSec: Int
    ) = withContext(Dispatchers.Default) {
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO daily_stats(date, cards_reviewed, xp_earned, study_time_sec)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(date) DO UPDATE SET
                    cards_reviewed = cards_reviewed + excluded.cards_reviewed,
                    xp_earned = xp_earned + excluded.xp_earned,
                    study_time_sec = study_time_sec + excluded.study_time_sec
            """.trimIndent(),
            parameters = 4
        ) {
            bindString(0, date)
            bindLong(1, cardsReviewed.toLong())
            bindLong(2, xpEarned.toLong())
            bindLong(3, studyTimeSec.toLong())
        }
        Unit
    }

    override suspend fun getDailyStats(date: String): DailyStatsData? = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT date, cards_reviewed, xp_earned, study_time_sec FROM daily_stats WHERE date = ?",
            mapper = { cursor ->
                val result = if (cursor.next().value) {
                    DailyStatsData(
                        date = cursor.getString(0) ?: "",
                        cardsReviewed = (cursor.getLong(1) ?: 0).toInt(),
                        xpEarned = (cursor.getLong(2) ?: 0).toInt(),
                        studyTimeSec = (cursor.getLong(3) ?: 0).toInt()
                    )
                } else null
                QueryResult.Value(result)
            },
            parameters = 1
        ) {
            bindString(0, date)
        }.value
    }

    override suspend fun getDailyStatsRange(startDate: String, endDate: String): List<DailyStatsData> =
        withContext(Dispatchers.Default) {
            driver.executeQuery(
                identifier = null,
                sql = "SELECT date, cards_reviewed, xp_earned, study_time_sec FROM daily_stats WHERE date >= ? AND date <= ? ORDER BY date",
                mapper = { cursor ->
                    val stats = mutableListOf<DailyStatsData>()
                    while (cursor.next().value) {
                        stats.add(
                            DailyStatsData(
                                date = cursor.getString(0) ?: "",
                                cardsReviewed = (cursor.getLong(1) ?: 0).toInt(),
                                xpEarned = (cursor.getLong(2) ?: 0).toInt(),
                                studyTimeSec = (cursor.getLong(3) ?: 0).toInt()
                            )
                        )
                    }
                    QueryResult.Value(stats.toList())
                },
                parameters = 2
            ) {
                bindString(0, startDate)
                bindString(1, endDate)
            }.value
        }
}

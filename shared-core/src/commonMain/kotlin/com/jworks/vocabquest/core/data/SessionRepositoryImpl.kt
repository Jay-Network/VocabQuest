package com.jworks.vocabquest.core.data

import com.jworks.vocabquest.core.domain.model.DailyStatsData
import com.jworks.vocabquest.core.domain.model.StudySession
import com.jworks.vocabquest.core.domain.repository.SessionRepository
import com.jworks.vocabquest.db.VocabQuestDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val database: VocabQuestDatabase
) : SessionRepository {

    private val driver get() = database.driver

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

        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT last_insert_rowid()",
            mapper = { cursor ->
                cursor.next()
                cursor.getLong(0)!!
            },
            parameters = 0
        )
        cursor.value
    }

    override suspend fun getRecentSessions(limit: Int): List<StudySession> = withContext(Dispatchers.Default) {
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT id, game_mode, started_at, cards_studied, correct_count, xp_earned, duration_sec FROM study_session ORDER BY started_at DESC LIMIT ?",
            mapper = { cursor ->
                val sessions = mutableListOf<StudySession>()
                while (cursor.next().value) {
                    sessions.add(
                        StudySession(
                            id = cursor.getLong(0)!!,
                            gameMode = cursor.getString(1)!!,
                            startedAt = cursor.getLong(2)!!,
                            cardsStudied = cursor.getLong(3)!!.toInt(),
                            correctCount = cursor.getLong(4)!!.toInt(),
                            xpEarned = cursor.getLong(5)!!.toInt(),
                            durationSec = cursor.getLong(6)!!.toInt()
                        )
                    )
                }
                sessions
            },
            parameters = 1
        ) {
            bindLong(0, limit.toLong())
        }
        cursor.value
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
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT date, cards_reviewed, xp_earned, study_time_sec FROM daily_stats WHERE date = ?",
            mapper = { cursor ->
                if (cursor.next().value) {
                    DailyStatsData(
                        date = cursor.getString(0)!!,
                        cardsReviewed = cursor.getLong(1)!!.toInt(),
                        xpEarned = cursor.getLong(2)!!.toInt(),
                        studyTimeSec = cursor.getLong(3)!!.toInt()
                    )
                } else null
            },
            parameters = 1
        ) {
            bindString(0, date)
        }
        cursor.value
    }

    override suspend fun getDailyStatsRange(startDate: String, endDate: String): List<DailyStatsData> =
        withContext(Dispatchers.Default) {
            val cursor = driver.executeQuery(
                identifier = null,
                sql = "SELECT date, cards_reviewed, xp_earned, study_time_sec FROM daily_stats WHERE date >= ? AND date <= ? ORDER BY date",
                mapper = { cursor ->
                    val stats = mutableListOf<DailyStatsData>()
                    while (cursor.next().value) {
                        stats.add(
                            DailyStatsData(
                                date = cursor.getString(0)!!,
                                cardsReviewed = cursor.getLong(1)!!.toInt(),
                                xpEarned = cursor.getLong(2)!!.toInt(),
                                studyTimeSec = cursor.getLong(3)!!.toInt()
                            )
                        )
                    }
                    stats
                },
                parameters = 2
            ) {
                bindString(0, startDate)
                bindString(1, endDate)
            }
            cursor.value
        }
}

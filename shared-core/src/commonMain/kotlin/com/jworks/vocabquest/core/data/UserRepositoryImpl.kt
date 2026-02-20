package com.jworks.vocabquest.core.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.jworks.vocabquest.core.domain.model.UserProfile
import com.jworks.vocabquest.core.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val driver: SqlDriver
) : UserRepository {

    override suspend fun getProfile(): UserProfile = withContext(Dispatchers.Default) {
        ensureProfileExists()
        driver.executeQuery(
            identifier = null,
            sql = "SELECT total_xp, level, current_streak, longest_streak, last_study_date, daily_goal, words_mastered FROM user_profile WHERE id = 1",
            mapper = { cursor ->
                val result = if (cursor.next().value) {
                    UserProfile(
                        totalXp = (cursor.getLong(0) ?: 0).toInt(),
                        level = (cursor.getLong(1) ?: 1).toInt(),
                        currentStreak = (cursor.getLong(2) ?: 0).toInt(),
                        longestStreak = (cursor.getLong(3) ?: 0).toInt(),
                        lastStudyDate = cursor.getString(4),
                        dailyGoal = (cursor.getLong(5) ?: 20).toInt()
                    )
                } else UserProfile()
                QueryResult.Value(result)
            },
            parameters = 0
        ).value
    }

    override suspend fun updateXpAndLevel(totalXp: Int, level: Int) = withContext(Dispatchers.Default) {
        ensureProfileExists()
        driver.execute(
            identifier = null,
            sql = "UPDATE user_profile SET total_xp = ?, level = ? WHERE id = 1",
            parameters = 2
        ) {
            bindLong(0, totalXp.toLong())
            bindLong(1, level.toLong())
        }
        Unit
    }

    override suspend fun updateStreak(current: Int, longest: Int, lastStudyDate: String) = withContext(Dispatchers.Default) {
        ensureProfileExists()
        driver.execute(
            identifier = null,
            sql = "UPDATE user_profile SET current_streak = ?, longest_streak = ?, last_study_date = ? WHERE id = 1",
            parameters = 3
        ) {
            bindLong(0, current.toLong())
            bindLong(1, longest.toLong())
            bindString(2, lastStudyDate)
        }
        Unit
    }

    override suspend fun updateDailyGoal(goal: Int) = withContext(Dispatchers.Default) {
        ensureProfileExists()
        driver.execute(
            identifier = null,
            sql = "UPDATE user_profile SET daily_goal = ? WHERE id = 1",
            parameters = 1
        ) {
            bindLong(0, goal.toLong())
        }
        Unit
    }

    private fun ensureProfileExists() {
        driver.execute(
            identifier = null,
            sql = "INSERT OR IGNORE INTO user_profile(id, total_xp, level, current_streak, longest_streak, daily_goal, words_mastered) VALUES (1, 0, 1, 0, 0, 20, 0)",
            parameters = 0
        )
    }
}

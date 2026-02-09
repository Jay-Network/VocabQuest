package com.jworks.vocabquest.core.domain.repository

interface AchievementRepository {
    suspend fun getAllAchievements(): List<Any>
}

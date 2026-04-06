package com.jworks.eigojourney.core.domain.repository

interface AchievementRepository {
    suspend fun getAllAchievements(): List<Any>
}

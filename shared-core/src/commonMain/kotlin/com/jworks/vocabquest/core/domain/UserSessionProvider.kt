package com.jworks.vocabquest.core.domain

interface UserSessionProvider {
    suspend fun getUserId(): String
    fun isPremium(): Boolean
}

package com.jworks.eigojourney.core.domain

interface UserSessionProvider {
    suspend fun getUserId(): String
    fun isPremium(): Boolean
}

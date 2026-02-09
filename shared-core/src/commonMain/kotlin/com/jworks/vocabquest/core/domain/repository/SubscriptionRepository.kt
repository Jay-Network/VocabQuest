package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.Subscription
import com.jworks.vocabquest.core.domain.model.SubscriptionPlan
import com.jworks.vocabquest.core.domain.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeSubscription(): Flow<Subscription>
    suspend fun getSubscription(): Subscription
    suspend fun getCurrentTier(): SubscriptionTier
    suspend fun isPremium(): Boolean
    suspend fun updatePlan(plan: SubscriptionPlan)
}

package com.jworks.eigojourney.core.domain.repository

import com.jworks.eigojourney.core.domain.model.Subscription
import com.jworks.eigojourney.core.domain.model.SubscriptionPlan
import com.jworks.eigojourney.core.domain.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeSubscription(): Flow<Subscription>
    suspend fun getSubscription(): Subscription
    suspend fun getCurrentTier(): SubscriptionTier
    suspend fun isPremium(): Boolean
    suspend fun updatePlan(plan: SubscriptionPlan)
}

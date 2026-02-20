package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.FeedbackCategory
import com.jworks.vocabquest.core.domain.model.FeedbackWithHistory
import com.jworks.vocabquest.core.domain.model.SubmitFeedbackResult

interface FeedbackRepository {
    suspend fun submitFeedback(
        email: String,
        appId: String,
        category: FeedbackCategory,
        feedbackText: String,
        deviceInfo: Map<String, String>? = null
    ): SubmitFeedbackResult

    suspend fun getFeedbackUpdates(
        email: String,
        appId: String,
        sinceId: Long? = null
    ): List<FeedbackWithHistory>

    suspend fun registerFcmToken(
        email: String,
        appId: String,
        fcmToken: String,
        deviceInfo: Map<String, String>? = null
    ): Boolean
}

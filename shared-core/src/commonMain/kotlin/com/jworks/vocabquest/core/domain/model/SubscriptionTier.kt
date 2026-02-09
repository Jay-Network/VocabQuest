package com.jworks.vocabquest.core.domain.model

enum class SubscriptionTier {
    FREE {
        override val maxWords = 500               // A1 level only
        override val maxReviewsPerDay = 20
        override val gameModes = setOf(GameMode.VOCABULARY)  // Flashcard only
        override val coinsEnabled = false
        override val audioEnabled = false
    },
    PREMIUM {
        override val maxWords = 10000              // All levels A1-C2
        override val maxReviewsPerDay = Int.MAX_VALUE
        override val gameModes = GameMode.entries.toSet()  // All modes
        override val coinsEnabled = true
        override val audioEnabled = true
    };

    abstract val maxWords: Int
    abstract val maxReviewsPerDay: Int
    abstract val gameModes: Set<GameMode>
    abstract val coinsEnabled: Boolean
    abstract val audioEnabled: Boolean

    companion object {
        fun fromPlan(plan: SubscriptionPlan): SubscriptionTier = when (plan) {
            SubscriptionPlan.FREE -> FREE
            SubscriptionPlan.PREMIUM -> PREMIUM
        }
    }
}

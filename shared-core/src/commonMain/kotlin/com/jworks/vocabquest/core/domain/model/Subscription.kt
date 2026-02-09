package com.jworks.vocabquest.core.domain.model

data class Subscription(
    val userId: String = LOCAL_USER_ID,
    val appId: String = "vocabquest",
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val cancelAtPeriodEnd: Boolean = false
)

enum class SubscriptionPlan(val value: String) {
    FREE("free"),
    PREMIUM("premium");

    companion object {
        fun fromString(value: String): SubscriptionPlan =
            entries.find { it.value == value } ?: FREE

        // Stripe product/price IDs (live mode)
        const val STRIPE_PRODUCT_ID = "prod_TwulWshGk8fROL"
        const val STRIPE_PRICE_MONTHLY = "price_1Sz0vuRwQ384lWsISQ46LLdO"  // $4.99/mo
        const val STRIPE_PRICE_YEARLY = "price_1Sz0w0RwQ384lWsIy4c0l2DN"   // $49.99/yr
    }
}

enum class SubscriptionStatus(val value: String) {
    ACTIVE("active"),
    PAST_DUE("past_due"),
    CANCELED("canceled"),
    TRIALING("trialing");

    companion object {
        fun fromString(value: String): SubscriptionStatus =
            entries.find { it.value == value } ?: ACTIVE
    }
}

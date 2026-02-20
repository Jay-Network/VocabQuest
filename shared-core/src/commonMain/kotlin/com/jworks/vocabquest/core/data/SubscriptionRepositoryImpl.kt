package com.jworks.vocabquest.core.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.jworks.vocabquest.core.domain.model.Subscription
import com.jworks.vocabquest.core.domain.model.SubscriptionPlan
import com.jworks.vocabquest.core.domain.model.SubscriptionTier
import com.jworks.vocabquest.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SubscriptionRepositoryImpl(
    private val driver: SqlDriver
) : SubscriptionRepository {

    private val _subscriptionFlow = MutableStateFlow(Subscription())
    private val initMutex = Mutex()
    @Volatile private var initialized = false

    private suspend fun ensureInitialized() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            withContext(Dispatchers.Default) {
                ensureTableExists()
                loadCached()
            }
            initialized = true
        }
    }

    private fun ensureTableExists() {
        driver.execute(
            identifier = null,
            sql = """
                CREATE TABLE IF NOT EXISTS subscription(
                    id INTEGER PRIMARY KEY DEFAULT 1,
                    plan TEXT NOT NULL DEFAULT 'free',
                    status TEXT NOT NULL DEFAULT 'active',
                    cancel_at_period_end INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent(),
            parameters = 0
        )
        driver.execute(
            identifier = null,
            sql = "INSERT OR IGNORE INTO subscription(id, plan, status, cancel_at_period_end) VALUES (1, 'free', 'active', 0)",
            parameters = 0
        )
    }

    private fun loadCached() {
        val sub = driver.executeQuery(
            identifier = null,
            sql = "SELECT plan, status, cancel_at_period_end FROM subscription WHERE id = 1",
            mapper = { cursor ->
                val result = if (cursor.next().value) {
                    Subscription(
                        plan = SubscriptionPlan.fromString(cursor.getString(0) ?: "free"),
                        status = com.jworks.vocabquest.core.domain.model.SubscriptionStatus.fromString(cursor.getString(1) ?: "active"),
                        cancelAtPeriodEnd = cursor.getLong(2) == 1L
                    )
                } else Subscription()
                QueryResult.Value(result)
            },
            parameters = 0
        ).value
        _subscriptionFlow.value = sub
    }

    override fun observeSubscription(): Flow<Subscription> = _subscriptionFlow

    override suspend fun getSubscription(): Subscription {
        ensureInitialized()
        return _subscriptionFlow.value
    }

    override suspend fun getCurrentTier(): SubscriptionTier {
        ensureInitialized()
        return SubscriptionTier.fromPlan(_subscriptionFlow.value.plan)
    }

    override suspend fun isPremium(): Boolean {
        ensureInitialized()
        return _subscriptionFlow.value.plan == SubscriptionPlan.PREMIUM
    }

    override suspend fun updatePlan(plan: SubscriptionPlan) {
        ensureInitialized()
        withContext(Dispatchers.Default) {
            driver.execute(
                identifier = null,
                sql = "UPDATE subscription SET plan = ? WHERE id = 1",
                parameters = 1
            ) {
                bindString(0, plan.value)
            }
            _subscriptionFlow.value = _subscriptionFlow.value.copy(plan = plan)
        }
    }
}

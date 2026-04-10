package com.jworks.eigojourney.android.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.eigojourney.core.domain.model.SubscriptionPlan
import com.jworks.eigojourney.core.domain.repository.SubscriptionRepository
import com.jworks.eigojourney.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyGoal: Int = 20,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val isPremium: Boolean = false,
    val planName: String = "Free",
    val appVersion: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("eigojourney_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val profile = userRepository.getProfile()
            val subscription = subscriptionRepository.getSubscription()
            val notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
            val reminderHour = prefs.getInt(KEY_REMINDER_HOUR, 9)
            val reminderMinute = prefs.getInt(KEY_REMINDER_MINUTE, 0)

            val versionName = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (_: Exception) { "" }

            _uiState.value = SettingsUiState(
                dailyGoal = profile.dailyGoal,
                notificationsEnabled = notificationsEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                isPremium = subscription.plan == SubscriptionPlan.PREMIUM,
                planName = if (subscription.plan == SubscriptionPlan.PREMIUM) "Premium" else "Free",
                appVersion = versionName,
                isLoading = false
            )
        }
    }

    fun updateDailyGoal(goal: Int) {
        val clamped = goal.coerceIn(5, 100)
        _uiState.value = _uiState.value.copy(dailyGoal = clamped)
        viewModelScope.launch {
            userRepository.updateDailyGoal(clamped)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
    }
}

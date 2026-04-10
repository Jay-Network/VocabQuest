package com.jworks.eigojourney.android.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.eigojourney.core.domain.model.Subscription
import com.jworks.eigojourney.core.domain.model.SubscriptionPlan
import com.jworks.eigojourney.core.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isPremium: Boolean = false,
    val subscription: Subscription? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        observeSubscription()
    }

    private fun observeSubscription() {
        viewModelScope.launch {
            subscriptionRepository.observeSubscription().collect { subscription ->
                _uiState.value = SubscriptionUiState(
                    isPremium = subscription.plan == SubscriptionPlan.PREMIUM,
                    subscription = subscription,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            // Force a re-read to update the flow
            subscriptionRepository.getSubscription()
        }
    }
}

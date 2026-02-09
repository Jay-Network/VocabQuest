package com.jworks.vocabquest.android.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jworks.vocabquest.core.domain.model.CoinBalance
import com.jworks.vocabquest.core.domain.model.LOCAL_USER_ID
import com.jworks.vocabquest.core.domain.model.PurchaseResult
import com.jworks.vocabquest.core.domain.model.ShopCategory
import com.jworks.vocabquest.core.domain.model.ShopItem
import com.jworks.vocabquest.core.domain.repository.JCoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val balance: CoinBalance = CoinBalance.empty(),
    val catalog: List<ShopItem> = emptyList(),
    val selectedCategory: ShopCategory? = null,
    val purchaseMessage: String? = null,
    val isLoading: Boolean = true
) {
    val categories: List<ShopCategory>
        get() = catalog.map { it.category }.distinct()

    val filteredItems: List<ShopItem>
        get() = if (selectedCategory == null) catalog
                else catalog.filter { it.category == selectedCategory }
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val jCoinRepository: JCoinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShop()
        observeBalance()
    }

    private fun loadShop() {
        viewModelScope.launch {
            val catalog = jCoinRepository.getShopCatalog()
            _uiState.value = _uiState.value.copy(
                catalog = catalog,
                isLoading = false
            )
        }
    }

    private fun observeBalance() {
        viewModelScope.launch {
            jCoinRepository.observeBalance(LOCAL_USER_ID).collect { balance ->
                _uiState.value = _uiState.value.copy(balance = balance)
            }
        }
    }

    fun selectCategory(category: ShopCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun purchaseItem(item: ShopItem) {
        viewModelScope.launch {
            val result = jCoinRepository.purchaseItem(LOCAL_USER_ID, item)
            val message = when (result) {
                is PurchaseResult.Success -> "Purchased ${item.name}!"
                is PurchaseResult.InsufficientFunds -> "Not enough coins (need ${result.required})"
                is PurchaseResult.AlreadyOwned -> "You already own ${item.name}"
                is PurchaseResult.Error -> "Error: ${result.message}"
            }
            _uiState.value = _uiState.value.copy(purchaseMessage = message)
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(purchaseMessage = null)
    }
}

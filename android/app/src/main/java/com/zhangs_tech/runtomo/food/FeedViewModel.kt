package com.zhangs_tech.runtomo.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.network.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FeedMessage {
    data class Fed(val foodName: String, val bond: Int, val newBondLevel: Int?) : FeedMessage
    data class Full(val foodName: String) : FeedMessage
    data class Bought(val foodName: String) : FeedMessage
    data class NotEnoughCoins(val distanceNeededM: Long) : FeedMessage
}

enum class FeedError { NoConnection, Failed }

data class FeedUiState(
    val isLoading: Boolean = true,
    val pantry: Pantry? = null,
    /** The food code of the request that runs now. */
    val busyCode: String? = null,
    val message: FeedMessage? = null,
    val error: FeedError? = null,
)

/** UC-18 Feed Tomo. */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val foodApi: FoodApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        run(busyCode = null) {
            val pantry = foodApi.getPantry()
            _uiState.update { it.copy(pantry = pantry) }
        }
    }

    fun buy(foodCode: String) = run(foodCode) {
        val name = foodName(foodCode)
        when (val attempt = foodApi.buy(foodCode)) {
            is BuyAttempt.Bought -> _uiState.update { it.copy(pantry = attempt.pantry, message = FeedMessage.Bought(name)) }
            is BuyAttempt.NotEnoughCoins ->
                _uiState.update { it.copy(message = FeedMessage.NotEnoughCoins(attempt.distanceNeededM)) }
        }
    }

    /** The app waits for the server before it shows eating, so it never shows eating and then "full". */
    fun feed(foodCode: String) {
        val tomo = _uiState.value.pantry?.tomo ?: return
        run(foodCode) {
            val food = _uiState.value.pantry?.items?.find { it.code == foodCode } ?: return@run
            when (val attempt = foodApi.feed(tomo.id, foodCode)) {
                is FeedAttempt.Fed -> {
                    val newLevel = attempt.pantry.tomo?.bondLevel?.takeIf { it > attempt.bondLevelBefore }
                    _uiState.update {
                        it.copy(pantry = attempt.pantry, message = FeedMessage.Fed(food.name, food.bond, newLevel))
                    }
                }
                FeedAttempt.Full -> _uiState.update {
                    it.copy(pantry = it.pantry?.copy(feedsLeftToday = 0), message = FeedMessage.Full(food.name))
                }
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun foodName(code: String) = _uiState.value.pantry?.items?.find { it.code == code }?.name.orEmpty()

    private fun run(busyCode: String?, block: suspend () -> Unit) {
        if (_uiState.value.busyCode != null) return
        _uiState.update { it.copy(busyCode = busyCode, message = null, error = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = if (e is ApiException.Network) FeedError.NoConnection else FeedError.Failed) }
            }
            _uiState.update { it.copy(busyCode = null, isLoading = false) }
        }
    }
}

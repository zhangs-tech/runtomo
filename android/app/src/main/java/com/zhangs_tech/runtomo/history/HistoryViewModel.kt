package com.zhangs_tech.runtomo.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.network.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val totals: PeriodTotals? = null,
    val days: List<HistoryDay> = emptyList(),
    val nextCursor: String? = null,
    val isLoadingMore: Boolean = false,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyApi: HistoryApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** UC-07 trigger: the Player opens History. It shows only the days the server received (BR-1). */
    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            try {
                // coroutineScope rethrows a failed request here, so the catch below sees it.
                val (totals, page) = coroutineScope {
                    val totals = async { historyApi.getTotals() }
                    val page = historyApi.getDays(cursor = null)
                    totals.await() to page
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadFailed = false,
                        totals = totals,
                        days = page.days,
                        nextCursor = page.nextCursor,
                    )
                }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, loadFailed = it.days.isEmpty()) }
            }
        }
    }

    /** UC-07 step 3: the Player scrolled to the end. */
    fun loadMore() {
        val cursor = _uiState.value.nextCursor ?: return
        if (_uiState.value.isLoadingMore) return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            try {
                val page = historyApi.getDays(cursor)
                _uiState.update { it.copy(days = it.days + page.days, nextCursor = page.nextCursor, isLoadingMore = false) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

}

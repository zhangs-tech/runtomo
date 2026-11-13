package com.zhangs_tech.runtomo.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.activity.ActivityRepository
import com.zhangs_tech.runtomo.cache.PlayerCache
import com.zhangs_tech.runtomo.core.GameDay
import com.zhangs_tech.runtomo.core.distanceM
import com.zhangs_tech.runtomo.network.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

/** Today's steps from the phone that the server does not have yet (UC-07 2b). */
data class UnsyncedToday(val date: String, val steps: Int, val distanceM: Int)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val totals: PeriodTotals? = null,
    val days: List<HistoryDay> = emptyList(),
    val unsyncedToday: UnsyncedToday? = null,
    val nextCursor: String? = null,
    val isLoadingMore: Boolean = false,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyApi: HistoryApi,
    private val activityRepository: ActivityRepository,
    private val playerCache: PlayerCache,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /** UC-07 trigger: the Player opens History. */
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
                        unsyncedToday = unsyncedToday(page.days),
                    )
                }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, loadFailed = it.days.isEmpty(), unsyncedToday = unsyncedToday(it.days)) }
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

    private suspend fun unsyncedToday(days: List<HistoryDay>): UnsyncedToday? {
        if (!activityRepository.canReadSteps()) return null
        val today = GameDay.today(clock).toString()
        val phoneSteps = try {
            activityRepository.readTodaySteps()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        val syncedSteps = days.firstOrNull { it.date == today }?.steps ?: 0
        if (phoneSteps <= syncedSteps) return null
        val strideM = playerCache.lastAcceptedSync()?.strideM ?: playerCache.home()?.strideM ?: return null
        return UnsyncedToday(today, phoneSteps, distanceM(phoneSteps, strideM))
    }
}

package com.zhangs_tech.runtomo.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.activity.ActivityRepository
import com.zhangs_tech.runtomo.activity.BackgroundSync
import com.zhangs_tech.runtomo.activity.Rewards
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.cache.PlayerCache
import com.zhangs_tech.runtomo.core.GameDay
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.quest.QuestApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** UC-03 1a: [NotGranted] before the Player answers, [Refused] after the Player refused. */
enum class StepsAccess { Unknown, Granted, NotGranted, Refused }

enum class HomeError { NoConnection, Failed }

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val home: HomeData? = null,
    val isOffline: Boolean = false,
    val stepsAccess: StepsAccess = StepsAccess.Unknown,
    val canReadInBackground: Boolean = true,
    /** UC-08 2a: the last sync was 2 or more days ago. */
    val showMissedYou: Boolean = false,
    /** Rewards for the reward screen (UC-06), or null when there is nothing to show. */
    val rewards: Rewards? = null,
    val claimingQuestCode: String? = null,
    val error: HomeError? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeApi: HomeApi,
    private val questApi: QuestApi,
    private val activityRepository: ActivityRepository,
    private val backgroundSync: BackgroundSync,
    private val playerCache: PlayerCache,
    private val authRepository: AuthRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val stepsPermissions: Set<String> get() = activityRepository.stepsPermissions

    private var loadJob: Job? = null
    private var checkedMissedYou = false

    /** UC-08 trigger: the Player opens the app or comes back to it. */
    fun onResume() = load(isRefresh = false)

    fun refresh() = load(isRefresh = true)

    fun onStepsPermissionResult(granted: Set<String>) {
        if (granted.isEmpty()) _uiState.update { it.copy(stepsAccess = StepsAccess.Refused) }
        load(isRefresh = false)
    }

    private fun load(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        _uiState.update { it.copy(isRefreshing = isRefresh, error = null) }
        loadJob = viewModelScope.launch {
            val phoneSteps = readPhoneSteps()
            try {
                showOnline(homeApi.getHome(), phoneSteps)
            } catch (e: ApiException.Unauthorized) {
                // The session ended. AuthRepository already shows the welcome screen.
            } catch (e: ApiException) {
                showOffline(phoneSteps)
            }
            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    /** Returns today's steps from Health Connect, or null if the app cannot read them. */
    private suspend fun readPhoneSteps(): Int? {
        val canRead = activityRepository.canReadSteps()
        val canReadInBackground = canRead && activityRepository.canReadInBackground()
        if (canReadInBackground) backgroundSync.schedule()
        _uiState.update {
            it.copy(
                stepsAccess = when {
                    canRead -> StepsAccess.Granted
                    it.stepsAccess == StepsAccess.Refused -> StepsAccess.Refused
                    else -> StepsAccess.NotGranted
                },
                canReadInBackground = canReadInBackground,
            )
        }
        if (!canRead) return null
        return try {
            activityRepository.readTodaySteps()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect read failed", e)
            null
        }
    }

    private suspend fun showOnline(fresh: HomeData, phoneSteps: Int?) {
        if (fresh.tomo == null) {
            authRepository.onStarterTomoMissing()
            return
        }
        // GET /home comes before the sync, so the last sync date is still the previous one (UC-08).
        val missedYou = !checkedMissedYou && fresh.lastSyncDate?.let(LocalDate::parse)
            ?.let { it <= GameDay.today(clock).minusDays(MISSED_YOU_DAYS) } == true
        checkedMissedYou = true

        val sync = phoneSteps?.let { activityRepository.sync(it) }
        val home = sync?.let(fresh::withSync) ?: fresh
        playerCache.saveHome(home)

        val pending = playerCache.pendingRewards()
        if (pending != null && !pending.showRewardScreen) playerCache.clearPendingRewards()
        _uiState.update {
            it.copy(
                home = home,
                isOffline = false,
                showMissedYou = it.showMissedYou || missedYou,
                rewards = pending?.takeIf { rewards -> rewards.showRewardScreen },
            )
        }
    }

    private suspend fun showOffline(phoneSteps: Int?) {
        val cached = playerCache.home()
        val strideM = playerCache.lastAcceptedSync()?.strideM ?: cached?.strideM
        val home = if (cached != null && strideM != null) {
            cached.offline(GameDay.today(clock).toString(), phoneSteps, strideM)
        } else {
            cached
        }
        _uiState.update {
            it.copy(home = home, isOffline = true, error = if (home == null) HomeError.NoConnection else null)
        }
    }

    fun claimQuest(questCode: String) {
        if (_uiState.value.claimingQuestCode != null) return
        _uiState.update { it.copy(claimingQuestCode = questCode, error = null) }
        viewModelScope.launch {
            try {
                val result = questApi.claim(questCode)
                val home = _uiState.value.home?.copy(coinBalance = result.coinBalance, quests = result.quests)
                home?.let { playerCache.saveHome(it) }
                _uiState.update { it.copy(home = home, claimingQuestCode = null) }
            } catch (e: ApiException) {
                val error = if (e is ApiException.Network) HomeError.NoConnection else HomeError.Failed
                _uiState.update { it.copy(claimingQuestCode = null, error = error) }
            }
        }
    }

    fun dismissRewards() {
        _uiState.update { it.copy(rewards = null) }
        viewModelScope.launch { playerCache.clearPendingRewards() }
    }

    fun dismissMissedYou() {
        _uiState.update { it.copy(showMissedYou = false) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun signOut() {
        backgroundSync.cancel()
        viewModelScope.launch { authRepository.signOut() }
    }

    private companion object {
        const val TAG = "HomeViewModel"
        const val MISSED_YOU_DAYS = 2L
    }
}

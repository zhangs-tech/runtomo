package com.zhangs_tech.runtomo.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.user.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SocialError { NoConnection, Failed }

private fun ApiException.toSocialError() = if (this is ApiException.Network) SocialError.NoConnection else SocialError.Failed

data class LeaderboardUiState(
    val period: LeaderboardPeriod = LeaderboardPeriod.WEEK,
    val isLoading: Boolean = true,
    val leaderboard: Leaderboard? = null,
    val error: SocialError? = null,
)

/** UC-16 View Friend Leaderboard. This week is the default (step 1). */
@HiltViewModel
class LeaderboardViewModel @Inject constructor(private val socialApi: SocialApi) : ViewModel() {
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        loadJob?.cancel()
        val period = _uiState.value.period
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            try {
                val leaderboard = socialApi.leaderboard(period)
                _uiState.update { it.copy(isLoading = false, leaderboard = leaderboard) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.toSocialError()) }
            }
        }
    }

    fun setPeriod(period: LeaderboardPeriod) {
        if (period == _uiState.value.period) return
        _uiState.update { it.copy(period = period, leaderboard = null) }
        load()
    }
}

data class FriendsUiState(
    val isLoading: Boolean = true,
    val requests: List<IncomingRequest> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val busyId: String? = null,
    val selectedFriend: Friend? = null,
    val limitReached: Boolean = false,
    val error: SocialError? = null,
)

/** UC-15 steps 6 and 7, 6a, and removing a friend. */
@HiltViewModel
class FriendsViewModel @Inject constructor(private val socialApi: SocialApi) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    fun load() = launch(busyId = null) {
        // coroutineScope rethrows a failed request here, so launch() sees it.
        val (requests, friends) = coroutineScope {
            val requests = async { socialApi.incomingRequests() }
            val friends = socialApi.friends()
            requests.await() to friends
        }
        _uiState.update { it.copy(requests = requests, friends = friends) }
    }

    fun accept(requestId: String) = launch(requestId) {
        val accepted = socialApi.accept(requestId)
        _uiState.update { it.copy(limitReached = !accepted) }
        if (accepted) reload()
    }

    fun decline(requestId: String) = launch(requestId) {
        socialApi.decline(requestId)
        _uiState.update { state -> state.copy(requests = state.requests.filterNot { it.id == requestId }) }
    }

    fun select(friend: Friend?) {
        _uiState.update { it.copy(selectedFriend = friend) }
    }

    fun remove(friend: Friend) = launch(friend.player.userId) {
        socialApi.remove(friend.player.userId)
        _uiState.update { state ->
            state.copy(friends = state.friends.filterNot { it.player.userId == friend.player.userId }, selectedFriend = null)
        }
    }

    private suspend fun reload() {
        val requests = socialApi.incomingRequests()
        val friends = socialApi.friends()
        _uiState.update { it.copy(requests = requests, friends = friends) }
    }

    private fun launch(busyId: String?, block: suspend () -> Unit) {
        if (_uiState.value.busyId != null) return
        _uiState.update { it.copy(busyId = busyId, error = null, limitReached = false) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = e.toSocialError()) }
            }
            _uiState.update { it.copy(busyId = null, isLoading = false) }
        }
    }
}

sealed interface AddFriendResult {
    data object NotFound : AddFriendResult
    data object OwnCode : AddFriendResult
    data object AlreadyFriends : AddFriendResult
    data object LimitReached : AddFriendResult
    data class Sent(val displayName: String) : AddFriendResult
    data class NowFriends(val displayName: String) : AddFriendResult
}

data class AddFriendUiState(
    val myFriendCode: String? = null,
    val code: String = "",
    val isBusy: Boolean = false,
    val preview: PlayerCard? = null,
    val result: AddFriendResult? = null,
    val error: SocialError? = null,
)

/** UC-15 steps 1 to 5, with 3a, 3b and 4a. */
@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val socialApi: SocialApi,
    private val userApi: UserApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddFriendUiState())
    val uiState: StateFlow<AddFriendUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val code = userApi.getMe().friendCode
                _uiState.update { it.copy(myFriendCode = code) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = e.toSocialError()) }
            }
        }
    }

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code, preview = null, result = null, error = null) }
    }

    fun lookUp() {
        val code = _uiState.value.code.trim()
        if (code.isEmpty()) return
        launch {
            val result = when (val lookUp = socialApi.lookUp(code)) {
                is LookUpResult.Found -> {
                    _uiState.update { it.copy(preview = lookUp.player) }
                    null
                }
                LookUpResult.NotFound -> AddFriendResult.NotFound
                LookUpResult.OwnCode -> AddFriendResult.OwnCode
                LookUpResult.AlreadyFriends -> AddFriendResult.AlreadyFriends
            }
            _uiState.update { it.copy(result = result) }
        }
    }

    fun sendRequest() {
        val player = _uiState.value.preview ?: return
        launch {
            val result = when (socialApi.sendRequest(player.userId)) {
                SendResult.SENT -> AddFriendResult.Sent(player.displayName)
                SendResult.NOW_FRIENDS -> AddFriendResult.NowFriends(player.displayName)
                SendResult.ALREADY_FRIENDS -> AddFriendResult.AlreadyFriends
                SendResult.LIMIT_REACHED -> AddFriendResult.LimitReached
            }
            _uiState.update { it.copy(result = result, preview = null, code = "") }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true, result = null, error = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: ApiException) {
                _uiState.update { it.copy(error = e.toSocialError()) }
            }
            _uiState.update { it.copy(isBusy = false) }
        }
    }
}

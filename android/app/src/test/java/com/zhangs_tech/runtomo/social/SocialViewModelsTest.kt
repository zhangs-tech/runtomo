package com.zhangs_tech.runtomo.social

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.user.FakeUserApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SocialViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val socialApi = FakeSocialApi()

    @Test
    fun leaderboard_loadsWeekThenSwitchesPeriod() {
        val viewModel = LeaderboardViewModel(socialApi)
        viewModel.load()

        viewModel.setPeriod(LeaderboardPeriod.TODAY)

        assertEquals(listOf(LeaderboardPeriod.WEEK, LeaderboardPeriod.TODAY), socialApi.leaderboardPeriods)
        assertEquals(1800L, viewModel.uiState.value.leaderboard!!.gapM)
    }

    @Test
    fun leaderboard_offline_showsError() {
        socialApi.error = ApiException.Network(IOException())
        val viewModel = LeaderboardViewModel(socialApi)

        viewModel.load()

        assertEquals(SocialError.NoConnection, viewModel.uiState.value.error)
    }

    @Test
    fun friends_acceptMovesRequestToFriends() {
        val viewModel = FriendsViewModel(socialApi)
        viewModel.load()

        viewModel.accept("r1")

        val state = viewModel.uiState.value
        assertTrue(state.requests.isEmpty())
        assertEquals(listOf("Ana", "Bo"), state.friends.map { it.player.displayName })
    }

    @Test
    fun friends_acceptAtLimit_showsLimit() {
        socialApi.acceptResult = false
        val viewModel = FriendsViewModel(socialApi)
        viewModel.load()

        viewModel.accept("r1")

        assertTrue(viewModel.uiState.value.limitReached)
        assertEquals(1, viewModel.uiState.value.requests.size)
    }

    @Test
    fun friends_declineAndRemove() {
        val viewModel = FriendsViewModel(socialApi)
        viewModel.load()

        viewModel.decline("r1")
        viewModel.select(viewModel.uiState.value.friends.single())
        viewModel.remove(viewModel.uiState.value.friends.single())

        val state = viewModel.uiState.value
        assertTrue(state.requests.isEmpty())
        assertTrue(state.friends.isEmpty())
        assertNull(state.selectedFriend)
    }

    @Test
    fun addFriend_showsOwnCodeThenLooksUpAndSends() {
        val viewModel = AddFriendViewModel(socialApi, FakeUserApi())
        assertEquals("00000001", viewModel.uiState.value.myFriendCode)

        viewModel.onCodeChange("12345678")
        viewModel.lookUp()
        assertEquals("Ana", viewModel.uiState.value.preview!!.displayName)

        viewModel.sendRequest()
        assertEquals(AddFriendResult.Sent("Ana"), viewModel.uiState.value.result)
        assertEquals("", viewModel.uiState.value.code)
    }

    @Test
    fun addFriend_requestBack_isNowFriends() {
        socialApi.sendResult = SendResult.NOW_FRIENDS
        val viewModel = AddFriendViewModel(socialApi, FakeUserApi())
        viewModel.onCodeChange("12345678")
        viewModel.lookUp()

        viewModel.sendRequest()

        assertEquals(AddFriendResult.NowFriends("Ana"), viewModel.uiState.value.result)
    }

    @Test
    fun addFriend_lookUpProblemsShowMessages() {
        val viewModel = AddFriendViewModel(socialApi, FakeUserApi())
        viewModel.onCodeChange("1")

        mapOf(
            LookUpResult.NotFound to AddFriendResult.NotFound,
            LookUpResult.OwnCode to AddFriendResult.OwnCode,
            LookUpResult.AlreadyFriends to AddFriendResult.AlreadyFriends,
        ).forEach { (lookUp, expected) ->
            socialApi.lookUpResult = lookUp
            viewModel.lookUp()
            assertEquals(expected, viewModel.uiState.value.result)
            assertNull(viewModel.uiState.value.preview)
        }
    }
}

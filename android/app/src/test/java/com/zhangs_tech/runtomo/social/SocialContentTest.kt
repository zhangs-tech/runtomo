package com.zhangs_tech.runtomo.social

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SocialContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    @Test
    fun leaderboard_highlightsMeAndShowsGap() {
        composeRule.setContent {
            LeaderboardContent(LeaderboardUiState(isLoading = false, leaderboard = FakeSocialApi().board), {}, {}, {})
        }

        composeRule.onNodeWithText(text(R.string.leaderboard_behind, "1.8", "Ana")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.leaderboard_you, "Kai")).assertIsDisplayed()
    }

    @Test
    fun leaderboard_withoutFriends_offersAddFriend() {
        var adds = 0
        val onlyMe = Leaderboard(listOf(LeaderboardRow(1, testPlayer("u1", "Kai"), 0, isMe = true)))
        composeRule.setContent {
            LeaderboardContent(LeaderboardUiState(isLoading = false, leaderboard = onlyMe), {}, {}, onAddFriend = { adds++ })
        }

        composeRule.onNodeWithText(text(R.string.leaderboard_empty)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.social_add_friend)).performClick()
        assertEquals(1, adds)
    }

    @Test
    fun friends_tapAccept_acceptsRequest() {
        var accepted: String? = null
        val api = FakeSocialApi()
        composeRule.setContent {
            FriendsContent(
                FriendsUiState(isLoading = false, requests = api.requests, friends = api.friendList),
                onRetry = {}, onAccept = { accepted = it }, onDecline = {}, onSelect = {}, onRemove = {},
            )
        }

        composeRule.onNodeWithText(text(R.string.friends_accept)).performClick()

        assertEquals("r1", accepted)
        composeRule.onNodeWithText(text(R.string.friends_distances, "1.0", "3.0", "9.0")).assertIsDisplayed()
    }

    @Test
    fun addFriend_showsCodeAndPreview() {
        var sends = 0
        composeRule.setContent {
            AddFriendContent(
                AddFriendUiState(myFriendCode = "00000001", code = "12345678", preview = testPlayer("u2", "Ana")),
                onCopy = {}, onShare = {}, onCodeChange = {}, onLookUp = {}, onSend = { sends++ },
            )
        }

        composeRule.onNodeWithText("00000001").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.friend_tomo, "Ana's Tomo")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.add_friend_send)).performClick()
        assertEquals(1, sends)
    }
}

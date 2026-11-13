package com.zhangs_tech.runtomo.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.activity.testRewards
import com.zhangs_tech.runtomo.quest.QuestStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private val home = testHome().let { home ->
        home.copy(
            today = Today("2026-11-14", 3734, 2800),
            quests = home.quests.mapIndexed { i, q -> if (i == 0) q.copy(progress = 2800, status = QuestStatus.CLAIMABLE) else q },
        )
    }

    private fun setContent(uiState: HomeUiState, actions: HomeActions = HomeActions()) {
        composeRule.setContent { HomeContent(uiState, actions) }
    }

    @Test
    fun showsTomoTodayAndGoal() {
        setContent(HomeUiState(isLoading = false, home = home, stepsAccess = StepsAccess.Granted))

        composeRule.onNodeWithText("Mochi").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.today_distance, "2.8", "5.0")).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.coin_balance, "0")).assertIsDisplayed()
    }

    @Test
    fun claimableQuest_tapClaim_claimsIt() {
        var claimed: String? = null
        setContent(
            HomeUiState(isLoading = false, home = home, stepsAccess = StepsAccess.Granted),
            HomeActions(onClaimQuest = { claimed = it }),
        )

        composeRule.onNodeWithText(text(R.string.quest_claim)).performScrollTo().performClick()

        assertEquals("walk-1km", claimed)
    }

    @Test
    fun noStepsAccess_tapAllow_requestsPermission() {
        var requests = 0
        setContent(
            HomeUiState(isLoading = false, home = home, stepsAccess = StepsAccess.NotGranted),
            HomeActions(onAllowSteps = { requests++ }),
        )

        composeRule.onNodeWithText(text(R.string.steps_access_allow)).performScrollTo().performClick()

        assertEquals(1, requests)
    }

    @Test
    fun refusedStepsAccess_showsHealthConnectLink() {
        setContent(HomeUiState(isLoading = false, home = home, stepsAccess = StepsAccess.Refused))

        composeRule.onNodeWithText(text(R.string.open_health_connect)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun offline_showsOfflineLabel() {
        setContent(HomeUiState(isLoading = false, home = home, isOffline = true))

        composeRule.onNodeWithText(text(R.string.offline_label)).assertIsDisplayed()
    }

    @Test
    fun missedYou_showsGreeting() {
        setContent(HomeUiState(isLoading = false, home = home, showMissedYou = true))

        composeRule.onNodeWithText(text(R.string.missed_you, "Mochi")).assertIsDisplayed()
    }

    @Test
    fun rewards_showsRewardScreenAndDone() {
        var dismissed = 0
        setContent(
            HomeUiState(isLoading = false, home = home, rewards = testRewards()),
            HomeActions(onDismissRewards = { dismissed++ }),
        )

        composeRule.onNodeWithText(text(R.string.rewards_walked, "Mochi", "1.2")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.rewards_bond_only, 12)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.quest_reward, 120)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.done)).performClick()
        assertEquals(1, dismissed)
    }

    @Test
    fun noHomeAndNoConnection_showsRetry() {
        var retries = 0
        setContent(
            HomeUiState(isLoading = false, error = HomeError.NoConnection),
            HomeActions(onRefresh = { retries++ }),
        )

        composeRule.onNodeWithText(text(R.string.error_no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.retry)).performClick()
        assertEquals(1, retries)
    }
}

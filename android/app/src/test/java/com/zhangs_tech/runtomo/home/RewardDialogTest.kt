package com.zhangs_tech.runtomo.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.activity.CompletedQuest
import com.zhangs_tech.runtomo.activity.testRewards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RewardDialogTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    @Test
    fun crossedMilestone_findsMilestoneInsideTheNewDistance() {
        assertEquals(10_000L, crossedMilestoneM(testRewards(distanceM = 500).copy(distanceTogetherM = 10_200)))
        assertNull(crossedMilestoneM(testRewards(distanceM = 500).copy(distanceTogetherM = 10_600)))
    }

    @Test
    fun levelUpQuestAndMilestone_areShown() {
        val rewards = testRewards().copy(
            bondLevelAfter = 2,
            distanceTogetherM = 10_400,
            completedQuests = listOf(CompletedQuest("walk-1km", "Walk or run 1 km", 100)),
        )
        composeRule.setContent { RewardContent(rewards, onDone = {}) }

        composeRule.onNodeWithText(text(R.string.rewards_level_up, 2)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.rewards_milestone, "10.0")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.rewards_quest_complete, "Walk or run 1 km")).assertIsDisplayed()
    }

    @Test
    fun coinCap_showsBondOnlyWithNote() {
        composeRule.setContent { RewardContent(testRewards().copy(coins = 0, coinCapReached = true), onDone = {}) }

        composeRule.onNodeWithText(text(R.string.rewards_bond_only, 12)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.rewards_coin_cap)).assertIsDisplayed()
    }
}

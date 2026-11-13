package com.zhangs_tech.runtomo.summon

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SummonContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private val idle = SummonUiState(isLoading = false, banner = testBanner())

    @Test
    fun banner_showsBalanceCostPityAndRates() {
        composeRule.setContent { SummonContent(idle, SummonActions()) }

        composeRule.onNodeWithText(text(R.string.summon_balance, "1,500")).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.summon_pity, 20)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Nova").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText(text(R.string.summon_rate, "10.0")).assertCountEquals(2)
    }

    @Test
    fun tapSummon_requestsSummon() {
        var requests = 0
        composeRule.setContent { SummonContent(idle, SummonActions(onSummon = { requests++ })) }

        composeRule.onNode(hasText(text(R.string.summon)) and hasClickAction()).performClick()

        assertEquals(1, requests)
    }

    @Test
    fun confirming_showsCost() {
        composeRule.setContent { SummonContent(idle.copy(phase = SummonPhase.Confirming), SummonActions()) }

        composeRule.onNodeWithText(text(R.string.summon_confirm_body, "1,000")).assertIsDisplayed()
    }

    @Test
    fun notEnoughCoins_showsDistanceNeeded() {
        composeRule.setContent { SummonContent(idle.copy(distanceNeededM = 3200), SummonActions()) }

        composeRule.onNodeWithText(text(R.string.summon_not_enough, "3.2")).assertIsDisplayed()
    }

    @Test
    fun hatching_callsHatchedAfterTheAnimation() {
        var hatched = 0
        composeRule.setContent {
            SummonContent(idle.copy(phase = SummonPhase.Hatching(testSummonResult())), SummonActions(onHatched = { hatched++ }))
        }

        composeRule.mainClock.advanceTimeBy(2_000)

        assertEquals(1, hatched)
    }

    @Test
    fun revealedNewTomo_showsEquipAndRename() {
        composeRule.setContent { SummonContent(idle.copy(phase = SummonPhase.Revealed(testSummonResult())), SummonActions()) }

        composeRule.onNodeWithText(text(R.string.summon_new_tomo)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.equip)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.rename)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun revealedDuplicate_showsConversion() {
        composeRule.setContent {
            SummonContent(idle.copy(phase = SummonPhase.Revealed(testSummonResult(duplicate = true))), SummonActions())
        }

        composeRule.onNodeWithText(text(R.string.summon_duplicate, "Nova", 200)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.equip)).assertDoesNotExist()
    }
}

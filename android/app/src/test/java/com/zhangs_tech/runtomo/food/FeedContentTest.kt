package com.zhangs_tech.runtomo.food

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
class FeedContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun setContent(uiState: FeedUiState, onFeed: (String) -> Unit = {}) {
        composeRule.setContent { FeedContent(uiState, onBack = {}, onRetry = {}, onBuy = {}, onFeed = onFeed) }
    }

    @Test
    fun showsFoodsAndFeedsLeft() {
        setContent(FeedUiState(isLoading = false, pantry = testPantry(feedsLeft = 2)))

        composeRule.onNodeWithText(text(R.string.feed_left_today, "Mochi", 2)).assertIsDisplayed()
        composeRule.onNodeWithText("Banana").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.feed_bond, 10)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun give_feedsTheFood() {
        var fed: String? = null
        setContent(FeedUiState(isLoading = false, pantry = testPantry()), onFeed = { fed = it })

        composeRule.onAllNodesWithText(text(R.string.feed_give))[0].performClick()

        assertEquals("banana", fed)
    }

    @Test
    fun foodNotOwned_cannotBeGiven() {
        setContent(FeedUiState(isLoading = false, pantry = testPantry()))

        composeRule.onAllNodesWithText(text(R.string.feed_give))[1].assertIsNotEnabled()
    }

    @Test
    fun fullTomo_showsFullMessage() {
        setContent(FeedUiState(isLoading = false, pantry = testPantry(feedsLeft = 0), message = FeedMessage.Full("Banana")))

        composeRule.onNodeWithText(text(R.string.feed_full, "Mochi", "Banana")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.feed_full_today, "Mochi")).assertIsDisplayed()
        composeRule.onAllNodesWithText(text(R.string.feed_give))[0].assertIsNotEnabled()
    }
}

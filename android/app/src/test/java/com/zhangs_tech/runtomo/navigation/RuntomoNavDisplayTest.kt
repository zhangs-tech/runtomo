package com.zhangs_tech.runtomo.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.HiltTestActivity
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.auth.SessionState
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RuntomoNavDisplayTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private fun text(id: Int) = composeRule.activity.getString(id)

    @Test
    fun signedOut_showsWelcome() {
        composeRule.setContent { RuntomoNavDisplay(SessionState.SignedOut) }

        composeRule.onNodeWithText(text(R.string.welcome_title)).assertIsDisplayed()
    }

    @Test
    fun needsStarterTomo_showsChooseStarter() {
        composeRule.setContent { RuntomoNavDisplay(SessionState.NeedsStarterTomo) }

        composeRule.onNodeWithText(text(R.string.choose_starter_title)).assertIsDisplayed()
    }

    @Test
    fun ready_showsHome() {
        composeRule.setContent { RuntomoNavDisplay(SessionState.Ready) }

        composeRule.onNodeWithText(text(R.string.home_title)).assertIsDisplayed()
    }

    @Test
    fun loading_showsNoScreen() {
        composeRule.setContent { RuntomoNavDisplay(SessionState.Loading) }

        composeRule.onNodeWithText(text(R.string.welcome_title)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.home_title)).assertDoesNotExist()
    }

    @Test
    fun sessionChange_replacesRootScreen() {
        var sessionState: SessionState by mutableStateOf(SessionState.SignedOut)
        composeRule.setContent { RuntomoNavDisplay(sessionState) }

        sessionState = SessionState.NeedsStarterTomo
        composeRule.onNodeWithText(text(R.string.choose_starter_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.welcome_title)).assertDoesNotExist()

        sessionState = SessionState.Ready
        composeRule.onNodeWithText(text(R.string.home_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.choose_starter_title)).assertDoesNotExist()
    }

    @Test
    fun backOnRootScreen_finishesActivity() {
        composeRule.setContent { RuntomoNavDisplay(SessionState.Ready) }

        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        assertTrue(composeRule.activity.isFinishing)
    }

    @Test
    fun stateRestoration_keepsCurrentScreen() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RuntomoNavDisplay(SessionState.Ready) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(text(R.string.home_title)).assertIsDisplayed()
    }
}

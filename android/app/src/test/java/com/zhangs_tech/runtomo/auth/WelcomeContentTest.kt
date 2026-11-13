package com.zhangs_tech.runtomo.auth

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
class WelcomeContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int) = composeRule.activity.getString(id)

    @Test
    fun idle_clickSignIn_callsCallback() {
        var clicks = 0
        composeRule.setContent { WelcomeContent(WelcomeUiState(), onSignInClick = { clicks++ }) }

        composeRule.onNodeWithText(text(R.string.sign_in_with_google)).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun signingIn_hidesSignInButton() {
        composeRule.setContent { WelcomeContent(WelcomeUiState(isSigningIn = true), onSignInClick = {}) }

        composeRule.onNodeWithText(text(R.string.sign_in_with_google)).assertDoesNotExist()
    }

    @Test
    fun error_showsMessageAndRetryButton() {
        composeRule.setContent {
            WelcomeContent(WelcomeUiState(error = SignInError.NoConnection), onSignInClick = {})
        }

        composeRule.onNodeWithText(text(R.string.error_no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.sign_in_with_google)).assertIsDisplayed()
    }
}

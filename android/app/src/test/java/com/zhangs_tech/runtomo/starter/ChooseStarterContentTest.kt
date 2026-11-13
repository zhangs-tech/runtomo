package com.zhangs_tech.runtomo.starter

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.tomo.FakeTomoApi
import com.zhangs_tech.runtomo.tomo.Species
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseStarterContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val starters = FakeTomoApi().starters

    private fun setContent(
        uiState: ChooseStarterUiState,
        onSelect: (Species) -> Unit = {},
        onConfirmSelection: () -> Unit = {},
        onRetryLoad: () -> Unit = {},
    ) {
        composeRule.setContent {
            ChooseStarterContent(
                uiState = uiState,
                onRetryLoad = onRetryLoad,
                onSelect = onSelect,
                onConfirmSelection = onConfirmSelection,
                onNicknameChange = {},
                onSave = {},
                onCancelNaming = {},
            )
        }
    }

    @Test
    fun pickStep_tapStarter_selectsIt() {
        var selected: Species? = null
        setContent(ChooseStarterUiState(starters = starters, isLoading = false), onSelect = { selected = it })

        composeRule.onNodeWithText("Rocky").performClick()

        assertEquals(starters[1], selected)
    }

    @Test
    fun pickStep_selectedStarter_showsDescriptionAndConfirm() {
        var confirms = 0
        setContent(
            ChooseStarterUiState(starters = starters, isLoading = false, selected = starters[0]),
            onConfirmSelection = { confirms++ },
        )

        composeRule.onNodeWithText(starters[0].description).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.choose_starter_confirm, "Luppy"))
            .performClick()
        assertEquals(1, confirms)
    }

    @Test
    fun namingStep_showsNicknameField() {
        setContent(
            ChooseStarterUiState(
                starters = starters,
                isLoading = false,
                selected = starters[0],
                isNaming = true,
                nickname = "Luppy",
            ),
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.nickname_title, "Luppy")).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.nickname_confirm)).assertIsDisplayed()
    }

    @Test
    fun loadFailed_tapRetry_retries() {
        var retries = 0
        setContent(
            ChooseStarterUiState(isLoading = false, error = StarterError.NoConnection),
            onRetryLoad = { retries++ },
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.error_no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.retry)).performClick()
        assertEquals(1, retries)
    }

    @Test
    fun greeting_saysHelloThenFinishes() {
        var finished = 0
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            ChooseStarterContent(
                uiState = ChooseStarterUiState(starters = starters, isLoading = false, greeting = Greeting(starters[0], "Mochi")),
                onRetryLoad = {}, onSelect = {}, onConfirmSelection = {}, onNicknameChange = {}, onSave = {}, onCancelNaming = {},
                onFinishGreeting = { finished++ },
            )
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.starter_hello, "Mochi")).assertIsDisplayed()
        assertEquals(0, finished)
        composeRule.mainClock.advanceTimeBy(4_000)
        assertEquals(1, finished)
    }

    @Test
    fun greeting_tapContinue_finishes() {
        var finished = 0
        composeRule.setContent {
            ChooseStarterContent(
                uiState = ChooseStarterUiState(starters = starters, isLoading = false, greeting = Greeting(starters[0], "Mochi")),
                onRetryLoad = {}, onSelect = {}, onConfirmSelection = {}, onNicknameChange = {}, onSave = {}, onCancelNaming = {},
                onFinishGreeting = { finished++ },
            )
        }
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.starter_hello_continue)).performClick()

        assertEquals(1, finished)
    }
}

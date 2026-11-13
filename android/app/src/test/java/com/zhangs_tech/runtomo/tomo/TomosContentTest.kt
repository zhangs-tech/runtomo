package com.zhangs_tech.runtomo.tomo

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TomosContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private val loaded = TomosUiState(isLoading = false, collection = FakeTomoApi().collection)

    @Test
    fun grid_showsProgressOwnedTomosAndSilhouettes() {
        composeRule.setContent { TomosContent(loaded, TomosActions()) }

        composeRule.onNodeWithText(text(R.string.tomos_collected, 2, 3)).assertIsDisplayed()
        composeRule.onNodeWithText("Mochi").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.unknown_species)).assertIsDisplayed()
    }

    @Test
    fun tapOwnedTomo_selectsIt() {
        var selected: String? = null
        composeRule.setContent { TomosContent(loaded, TomosActions(onSelect = { selected = it })) }

        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pebble"))
        composeRule.onNodeWithText("Pebble").performClick()

        assertEquals("rocky", selected)
    }

    @Test
    fun equippedTomoCard_disablesEquip() {
        composeRule.setContent { TomosContent(loaded.copy(selectedSpeciesCode = "luppy"), TomosActions()) }

        composeRule.onNodeWithText(text(R.string.profile_bond_level)).assertIsDisplayed()
        composeRule.onNode(hasText("1") and hasAnyAncestor(isDialog())).assertIsDisplayed()
        composeRule.onNode(hasText(text(R.string.equipped)) and hasClickAction() and hasAnyAncestor(isDialog())).assertIsNotEnabled()
    }

    @Test
    fun otherTomoCard_tapEquip_equipsIt() {
        var equipped: String? = null
        composeRule.setContent {
            TomosContent(loaded.copy(selectedSpeciesCode = "rocky"), TomosActions(onEquip = { equipped = it }))
        }

        composeRule.onNodeWithText(text(R.string.equip)).performClick()

        assertEquals("t2", equipped)
    }

    @Test
    fun silhouetteCard_showsNameRarityAndSummon() {
        var summons = 0
        composeRule.setContent {
            TomosContent(loaded.copy(selectedSpeciesCode = "nova"), TomosActions(onOpenSummon = { summons++ }))
        }

        composeRule.onNodeWithText("Nova").assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.rarity_epic)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.not_owned_hint, "Nova")).assertIsDisplayed()
    }
}

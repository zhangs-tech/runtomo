package com.zhangs_tech.runtomo.tomo

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.network.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class TomosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tomoApi = FakeTomoApi()
    private val viewModel by lazy { TomosViewModel(tomoApi).apply { load() } }
    private val state get() = viewModel.uiState.value

    @Test
    fun load_showsCollection() {
        assertFalse(state.isLoading)
        assertEquals(tomoApi.collection, state.collection)
    }

    @Test
    fun sortOrders_sortOwnedTomosFirst() {
        viewModel.setSortOrder(SortOrder.BOND)
        assertEquals(listOf("rocky", "luppy", "nova"), state.sortedEntries.map { it.species.code })

        viewModel.setSortOrder(SortOrder.DISTANCE)
        assertEquals(listOf("luppy", "rocky", "nova"), state.sortedEntries.map { it.species.code })

        viewModel.setSortOrder(SortOrder.RARITY)
        assertEquals("nova", state.sortedEntries.first().species.code)
    }

    @Test
    fun equip_updatesEquippedTomo() {
        viewModel.select("rocky")

        viewModel.equip("t2")

        assertEquals(listOf("t2"), tomoApi.equipped)
        assertEquals("t2", state.collection!!.equippedTomoId)
    }

    @Test
    fun rename_savesTrimmedNickname() {
        viewModel.select("luppy")
        viewModel.startRename()

        viewModel.rename("t1", "  Kiki ")

        assertEquals("Kiki", state.selectedEntry!!.tomo!!.nickname)
        assertFalse(state.isRenaming)
    }

    @Test
    fun rename_invalidNickname_showsRule() {
        viewModel.rename("t1", " ")

        assertEquals(TomosError.InvalidNickname, state.error)
    }

    @Test
    fun equip_offline_showsNoConnection() {
        viewModel.select("rocky")
        tomoApi.error = ApiException.Network(IOException())

        viewModel.equip("t2")

        assertEquals(TomosError.NoConnection, state.error)
        assertEquals("t1", state.collection!!.equippedTomoId)
    }

    @Test
    fun dismiss_closesCard() {
        viewModel.select("nova")
        assertEquals(testNova, state.selectedEntry!!.species)

        viewModel.dismiss()

        assertNull(state.selectedEntry)
    }
}

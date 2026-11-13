package com.zhangs_tech.runtomo.history

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.network.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val historyApi = FakeHistoryApi()
    private val viewModel by lazy { HistoryViewModel(historyApi) }
    private val state get() = viewModel.uiState.value

    @Test
    fun load_showsTotalsAndFirstPage() {
        viewModel.load()

        assertFalse(state.isLoading)
        assertEquals(historyApi.totals, state.totals)
        assertEquals(listOf("2026-11-14", "2026-11-13"), state.days.map { it.date })
    }

    @Test
    fun loadMore_appendsOlderDaysUntilTheEnd() {
        viewModel.load()

        viewModel.loadMore()
        viewModel.loadMore()

        assertEquals(listOf("2026-11-14", "2026-11-13", "2026-11-12"), state.days.map { it.date })
        assertNull(state.nextCursor)
    }

    @Test
    fun offlineWithoutData_showsRetry() {
        historyApi.error = ApiException.Network(IOException())

        viewModel.load()

        assertTrue(state.loadFailed)
    }

    @Test
    fun offlineAfterLoad_keepsShownDays() {
        viewModel.load()
        historyApi.error = ApiException.Network(IOException())

        viewModel.load()

        assertFalse(state.loadFailed)
        assertEquals(2, state.days.size)
    }
}

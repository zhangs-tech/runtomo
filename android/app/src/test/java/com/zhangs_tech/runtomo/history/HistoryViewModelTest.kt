package com.zhangs_tech.runtomo.history

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.activity.ActivityRepository
import com.zhangs_tech.runtomo.activity.FakeActivityApi
import com.zhangs_tech.runtomo.activity.FakeStepsReader
import com.zhangs_tech.runtomo.cache.AcceptedSync
import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.testClock
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
    private val stepsReader = FakeStepsReader(steps = 1600)
    private val playerCache = FakePlayerCache().apply { acceptedSync = AcceptedSync("2026-11-14", 1600, 0.75) }
    private val viewModel = HistoryViewModel(
        historyApi,
        ActivityRepository(stepsReader, FakeActivityApi(), playerCache, testClock),
        playerCache,
        testClock,
    )
    private val state get() = viewModel.uiState.value

    @Test
    fun load_showsTotalsAndFirstPage() {
        viewModel.load()

        assertFalse(state.isLoading)
        assertEquals(historyApi.totals, state.totals)
        assertEquals(listOf("2026-11-14", "2026-11-13"), state.days.map { it.date })
        assertNull(state.unsyncedToday)
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
    fun phoneHasMoreStepsThanServer_showsWaitingToSync() {
        stepsReader.steps = 2000

        viewModel.load()

        assertEquals(UnsyncedToday("2026-11-14", 2000, 1500), state.unsyncedToday)
    }

    @Test
    fun offlineWithoutData_showsRetryAndPhoneSteps() {
        historyApi.error = ApiException.Network(IOException())

        viewModel.load()

        assertTrue(state.loadFailed)
        assertEquals(1600, state.unsyncedToday!!.steps)
    }
}

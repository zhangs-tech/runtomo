package com.zhangs_tech.runtomo.activity

import com.zhangs_tech.runtomo.cache.AcceptedSync
import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.testClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

class ActivityRepositoryTest {
    private val stepsReader = FakeStepsReader()
    private val activityApi = FakeActivityApi()
    private val playerCache = FakePlayerCache()
    private val repository = ActivityRepository(stepsReader, activityApi, playerCache, testClock)

    @Test
    fun readTodaySteps_readsFromStartOfUtcPlus7DayToNow() = runTest {
        assertEquals(1600, repository.readTodaySteps())
        assertEquals(Instant.parse("2026-11-13T17:00:00Z") to testClock.instant(), stepsReader.lastRange)
    }

    @Test
    fun sync_sendsTodayAndSavesAcceptedTotalAndRewards() = runTest {
        val result = repository.sync(1600)

        assertEquals(listOf(LocalDate.of(2026, 11, 14) to 1600), activityApi.syncs)
        assertEquals(1200, result!!.distanceM)
        assertEquals(AcceptedSync("2026-11-14", 1600, 0.75), playerCache.acceptedSync)
        assertEquals(testRewards(), playerCache.pending)
    }

    @Test
    fun sync_unchangedTotal_skipsRequestWhenAsked() = runTest {
        playerCache.acceptedSync = AcceptedSync("2026-11-14", 1600, 0.75)

        assertNull(repository.sync(1600, skipIfUnchanged = true))
        assertEquals(emptyList<Pair<LocalDate, Int>>(), activityApi.syncs)
    }

    @Test
    fun sync_sameTotalFromYesterday_isSent() = runTest {
        playerCache.acceptedSync = AcceptedSync("2026-11-13", 1600, 0.75)

        repository.sync(1600, skipIfUnchanged = true)

        assertEquals(1, activityApi.syncs.size)
    }

    @Test
    fun sync_offline_returnsNullAndKeepsCache() = runTest {
        activityApi.error = ApiException.Network(IOException())

        assertNull(repository.sync(1600))
        assertNull(playerCache.acceptedSync)
        assertNull(playerCache.pending)
    }

    @Test
    fun sync_twiceWithRewards_joinsPendingRewards() = runTest {
        repository.sync(1600)
        repository.sync(2000)

        assertEquals(2400, playerCache.pending!!.distanceM)
    }
}

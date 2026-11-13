package com.zhangs_tech.runtomo.cache

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zhangs_tech.runtomo.activity.testRewards
import com.zhangs_tech.runtomo.home.testHome
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStorePlayerCacheTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val cache = DataStorePlayerCache(
        PreferenceDataStoreFactory.create(scope = testScope.backgroundScope) {
            temporaryFolder.newFile("player.preferences_pb").also { it.delete() }
        },
    )

    @Test
    fun emptyCache_returnsNull() = testScope.runTest {
        assertNull(cache.lastAcceptedSync())
        assertNull(cache.pendingRewards())
        assertNull(cache.home())
    }

    @Test
    fun savedValues_areReadBack() = testScope.runTest {
        cache.saveAcceptedSync(AcceptedSync("2026-11-14", 1600, 0.75))
        cache.saveHome(testHome())

        assertEquals(AcceptedSync("2026-11-14", 1600, 0.75), cache.lastAcceptedSync())
        assertEquals(testHome(), cache.home())
    }

    @Test
    fun addPendingRewards_joinsWithEarlierRewards() = testScope.runTest {
        cache.addPendingRewards(testRewards(distanceM = 300))
        cache.addPendingRewards(testRewards(distanceM = 400))

        assertEquals(700, cache.pendingRewards()!!.distanceM)

        cache.clearPendingRewards()
        assertNull(cache.pendingRewards())
    }

    @Test
    fun clear_removesEverything() = testScope.runTest {
        cache.saveHome(testHome())
        cache.addPendingRewards(testRewards())

        cache.clear()

        assertNull(cache.home())
        assertNull(cache.pendingRewards())
    }
}

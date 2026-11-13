package com.zhangs_tech.runtomo.activity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.zhangs_tech.runtomo.auth.AuthTokens
import com.zhangs_tech.runtomo.auth.FakeTokenStore
import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.testClock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StepSyncWorkerTest {
    private val stepsReader = FakeStepsReader()
    private val activityApi = FakeActivityApi()
    private val tokenStore = FakeTokenStore(AuthTokens("a", "r"))
    private val backgroundSync = FakeBackgroundSync().apply { schedule() }

    private fun worker(): StepSyncWorker {
        val repository = ActivityRepository(stepsReader, activityApi, FakePlayerCache(), testClock)
        val factory = object : WorkerFactory() {
            override fun createWorker(context: Context, name: String, params: WorkerParameters): ListenableWorker =
                StepSyncWorker(context, params, repository, tokenStore, backgroundSync)
        }
        return TestListenableWorkerBuilder<StepSyncWorker>(ApplicationProvider.getApplicationContext())
            .setWorkerFactory(factory)
            .build()
    }

    @Test
    fun doWork_syncsTodaySteps() = runTest {
        assertEquals(ListenableWorker.Result.success(), worker().doWork())

        assertEquals(1600, activityApi.syncs.single().second)
    }

    @Test
    fun doWork_signedOut_cancelsItself() = runTest {
        tokenStore.tokens = null

        worker().doWork()

        assertFalse(backgroundSync.isScheduled)
        assertEquals(0, activityApi.syncs.size)
    }

    @Test
    fun doWork_backgroundPermissionRemoved_cancelsItself() = runTest {
        stepsReader.canReadInBackground = false

        worker().doWork()

        assertFalse(backgroundSync.isScheduled)
        assertEquals(0, activityApi.syncs.size)
    }
}

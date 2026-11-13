package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.activity.ActivityRepository
import com.zhangs_tech.runtomo.activity.FakeActivityApi
import com.zhangs_tech.runtomo.activity.FakeBackgroundSync
import com.zhangs_tech.runtomo.activity.FakeStepsReader
import com.zhangs_tech.runtomo.activity.testRewards
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.auth.AuthTokens
import com.zhangs_tech.runtomo.auth.FakeAuthApi
import com.zhangs_tech.runtomo.auth.FakeTokenStore
import com.zhangs_tech.runtomo.auth.SessionState
import com.zhangs_tech.runtomo.cache.AcceptedSync
import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.quest.QuestStatus
import com.zhangs_tech.runtomo.testClock
import com.zhangs_tech.runtomo.user.FakeUserApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val homeApi = FakeHomeApi()
    private val questApi = FakeQuestApi()
    private val stepsReader = FakeStepsReader()
    private val activityApi = FakeActivityApi()
    private val backgroundSync = FakeBackgroundSync()
    private val playerCache = FakePlayerCache()
    private val tokenStore = FakeTokenStore(AuthTokens("a", "r"))
    private val authRepository = AuthRepository(FakeAuthApi(), tokenStore, FakeUserApi(), playerCache)
    private val viewModel = HomeViewModel(
        homeApi,
        questApi,
        ActivityRepository(stepsReader, activityApi, playerCache, testClock),
        backgroundSync,
        playerCache,
        authRepository,
        testClock,
    )
    private val state get() = viewModel.uiState.value

    @Test
    fun onResume_loadsHomeSyncsStepsAndShowsRewards() {
        viewModel.onResume()

        assertFalse(state.isLoading)
        assertFalse(state.isOffline)
        assertEquals(1600, state.home!!.today.steps)
        assertEquals(120, state.home!!.coinBalance)
        assertEquals(QuestStatus.CLAIMABLE, state.home!!.quests[0].status)
        assertEquals(testRewards(), state.rewards)
        assertEquals(state.home, playerCache.cachedHome)
        assertTrue(backgroundSync.isScheduled)
    }

    @Test
    fun smallSync_updatesNumbersWithoutRewardScreen() {
        activityApi.rewards = testRewards(distanceM = 100, showRewardScreen = false)

        viewModel.onResume()

        assertNull(state.rewards)
        assertNull(playerCache.pending)
    }

    @Test
    fun dismissRewards_clearsPendingRewards() {
        viewModel.onResume()

        viewModel.dismissRewards()

        assertNull(state.rewards)
        assertNull(playerCache.pending)
    }

    @Test
    fun noStepsPermission_showsAccessCardAndDoesNotSync() {
        stepsReader.canRead = false

        viewModel.onResume()

        assertEquals(StepsAccess.NotGranted, state.stepsAccess)
        assertEquals(emptyList<Any>(), activityApi.syncs)
        assertFalse(backgroundSync.isScheduled)
    }

    @Test
    fun refusedPermission_showsRefused() {
        stepsReader.canRead = false

        viewModel.onStepsPermissionResult(emptySet())

        assertEquals(StepsAccess.Refused, state.stepsAccess)
    }

    @Test
    fun noBackgroundRead_showsNoteAndDoesNotSchedule() {
        stepsReader.canReadInBackground = false

        viewModel.onResume()

        assertEquals(StepsAccess.Granted, state.stepsAccess)
        assertFalse(state.canReadInBackground)
        assertFalse(backgroundSync.isScheduled)
    }

    @Test
    fun offline_showsCacheWithPhoneSteps() {
        playerCache.cachedHome = testHome()
        playerCache.acceptedSync = AcceptedSync("2026-11-14", 0, 0.75)
        homeApi.error = ApiException.Network(IOException())
        stepsReader.steps = 2000

        viewModel.onResume()

        assertTrue(state.isOffline)
        assertEquals(Today("2026-11-14", 2000, 1500), state.home!!.today)
        assertNull(state.error)
    }

    @Test
    fun offlineWithoutCache_showsNoConnection() {
        homeApi.error = ApiException.Network(IOException())

        viewModel.onResume()

        assertNull(state.home)
        assertEquals(HomeError.NoConnection, state.error)
    }

    @Test
    fun lastSyncTwoDaysAgo_showsMissedYouOnce() {
        homeApi.home = testHome(lastSyncDate = "2026-11-12")

        viewModel.onResume()
        assertTrue(state.showMissedYou)

        viewModel.dismissMissedYou()
        viewModel.onResume()
        assertFalse(state.showMissedYou)
    }

    @Test
    fun lastSyncYesterday_showsNoMissedYou() {
        homeApi.home = testHome(lastSyncDate = "2026-11-13")

        viewModel.onResume()

        assertFalse(state.showMissedYou)
    }

    @Test
    fun homeWithoutTomo_sendsPlayerToStarter() {
        homeApi.home = testHome().copy(tomo = null)

        viewModel.onResume()

        assertEquals(SessionState.NeedsStarterTomo, authRepository.sessionState.value)
    }

    @Test
    fun claimQuest_updatesBalanceAndQuests() {
        viewModel.onResume()

        viewModel.claimQuest("walk-1km")

        assertEquals(listOf("walk-1km"), questApi.claimed)
        assertEquals(220, state.home!!.coinBalance)
        assertEquals(QuestStatus.CLAIMED, state.home!!.quests[0].status)
        assertNull(state.claimingQuestCode)
    }

    @Test
    fun claimQuest_offline_showsError() {
        viewModel.onResume()
        questApi.error = ApiException.Network(IOException())

        viewModel.claimQuest("walk-1km")

        assertEquals(HomeError.NoConnection, state.error)
    }

    @Test
    fun signOut_cancelsBackgroundSyncAndClearsData() {
        viewModel.onResume()

        viewModel.signOut()

        assertFalse(backgroundSync.isScheduled)
        assertEquals(SessionState.SignedOut, authRepository.sessionState.value)
        assertNull(tokenStore.tokens)
        assertNull(playerCache.cachedHome)
    }
}

package com.zhangs_tech.runtomo.summon

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.tomo.FakeTomoApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SummonViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val summonApi = FakeSummonApi()
    private val tomoApi = FakeTomoApi()
    private val viewModel by lazy { SummonViewModel(summonApi, tomoApi) }
    private val state get() = viewModel.uiState.value

    @Test
    fun load_showsBanner() {
        assertEquals(testBanner(), state.banner)
    }

    @Test
    fun summon_asksToConfirmThenHatchesAndReveals() {
        viewModel.requestSummon()
        assertEquals(SummonPhase.Confirming, state.phase)

        viewModel.confirmSummon()
        assertEquals(SummonPhase.Hatching(testSummonResult()), state.phase)
        assertEquals(500, state.banner!!.coinBalance)

        viewModel.onHatched()
        assertEquals(SummonPhase.Revealed(testSummonResult()), state.phase)
    }

    @Test
    fun notEnoughCoins_skipsConfirmAndShowsDistanceNeeded() {
        summonApi.banner = testBanner(coinBalance = 700)
        summonApi.attempt = SummonAttempt.NotEnoughCoins(shortfall = 300, distanceNeededM = 3000)

        viewModel.requestSummon()

        assertEquals(SummonPhase.Idle, state.phase)
        assertEquals(3000L, state.distanceNeededM)
    }

    @Test
    fun lostConnection_withNewSummonOnServer_showsRealResult() {
        viewModel.requestSummon()
        summonApi.summonError = ApiException.Network(IOException())
        summonApi.latestResult = testSummonResult(id = "s2")

        viewModel.confirmSummon()

        assertEquals(SummonPhase.Hatching(testSummonResult(id = "s2")), state.phase)
    }

    @Test
    fun lostConnection_withoutNewSummon_saysCoinsWereNotSpent() {
        viewModel.requestSummon()
        summonApi.summonError = ApiException.Network(IOException())

        viewModel.confirmSummon()

        assertEquals(SummonPhase.Idle, state.phase)
        assertEquals(SummonError.LostConnection, state.error)
    }

    @Test
    fun lostConnection_andCheckFails_letsPlayerCheckAgain() {
        viewModel.requestSummon()
        summonApi.summonError = ApiException.Network(IOException())
        summonApi.latestError = ApiException.Network(IOException())

        viewModel.confirmSummon()
        assertEquals(SummonPhase.CheckFailed, state.phase)

        summonApi.latestError = null
        summonApi.latestResult = testSummonResult(id = "s3")
        viewModel.checkResult()
        assertEquals(SummonPhase.Hatching(testSummonResult(id = "s3")), state.phase)
    }

    @Test
    fun revealedTomo_canBeEquippedAndRenamed() {
        viewModel.requestSummon()
        viewModel.confirmSummon()
        viewModel.onHatched()

        viewModel.equip()
        viewModel.startRename()
        viewModel.rename("Star")

        assertEquals(listOf("t9"), tomoApi.equipped)
        assertTrue(state.isEquipped)
        assertEquals("Star", (state.phase as SummonPhase.Revealed).result.tomo!!.nickname)
    }

    @Test
    fun done_returnsToBanner() {
        viewModel.requestSummon()
        viewModel.confirmSummon()
        viewModel.onHatched()

        viewModel.done()

        assertEquals(SummonPhase.Idle, state.phase)
    }
}

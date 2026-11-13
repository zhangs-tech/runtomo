package com.zhangs_tech.runtomo.food

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.network.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class FeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodApi = FakeFoodApi()
    private val viewModel by lazy { FeedViewModel(foodApi) }
    private val state get() = viewModel.uiState.value

    @Test
    fun load_showsPantry() {
        assertEquals(testPantry(), state.pantry)
        assertNull(state.busyCode)
    }

    @Test
    fun feed_showsEatingMessageWithBond() {
        viewModel.feed("banana")

        assertEquals(listOf("t1" to "banana"), foodApi.fed)
        assertEquals(FeedMessage.Fed("Banana", 3, newBondLevel = null), state.message)
        assertEquals(2, state.pantry!!.feedsLeftToday)
    }

    @Test
    fun feed_levelUp_showsNewLevel() {
        foodApi.feedAttempt = FeedAttempt.Fed(bondLevelBefore = 1, pantry = testPantry(bondPoints = 50, bondLevel = 2))

        viewModel.feed("banana")

        assertEquals(FeedMessage.Fed("Banana", 3, newBondLevel = 2), state.message)
    }

    @Test
    fun feed_full_keepsFoodAndShowsFull() {
        foodApi.feedAttempt = FeedAttempt.Full

        viewModel.feed("banana")

        assertEquals(FeedMessage.Full("Banana"), state.message)
        assertEquals(1, state.pantry!!.items.first().quantity)
        assertEquals(0, state.pantry!!.feedsLeftToday)
    }

    @Test
    fun buy_addsFood() {
        viewModel.buy("banana")

        assertEquals(FeedMessage.Bought("Banana"), state.message)
        assertEquals(2, state.pantry!!.items.first().quantity)
    }

    @Test
    fun buy_notEnoughCoins_showsDistanceNeeded() {
        foodApi.buyAttempt = BuyAttempt.NotEnoughCoins(shortfall = 50, distanceNeededM = 500)

        viewModel.buy("bento")

        assertEquals(FeedMessage.NotEnoughCoins(500), state.message)
    }

    @Test
    fun offline_showsNoConnection() {
        viewModel.uiState.value
        foodApi.error = ApiException.Network(IOException())

        viewModel.feed("banana")

        assertEquals(FeedError.NoConnection, state.error)
        assertNull(state.busyCode)
    }
}

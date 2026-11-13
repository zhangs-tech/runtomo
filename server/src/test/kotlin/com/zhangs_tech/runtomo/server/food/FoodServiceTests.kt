package com.zhangs_tech.runtomo.server.food

import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.coin.NotEnoughCoinsException
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
import com.zhangs_tech.runtomo.server.tomo.TomoNotFoundException
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class FoodServiceTests {
    private val userRepository = InMemoryUserRepository()
    private val tomoRepository = InMemoryTomoRepository()
    private val coinRepository = InMemoryCoinRepository(userRepository)
    private val inventory = InMemoryFoodInventoryRepository()
    private val feedings = InMemoryFeedingRepository()
    private val service = FoodService(
        testGameData, GameCalendar(testDaytimeClock), RewardRules(testGameData),
        userRepository, tomoRepository, coinRepository, inventory, feedings,
    )
    private val user = userRepository.create("sub-1", "Runner0001", "00000001")!!
    private val tomo = tomoRepository.create(user.id, "luppy", "Mochi", LocalDate.of(2026, 11, 1)).also {
        userRepository.setEquippedTomo(user.id, it.id)
    }

    @Test
    fun `pantry lists every food with the quantity and feeds left`() {
        inventory.add(user.id, "banana")

        val pantry = service.pantry(user.id)

        assertEquals(listOf("banana" to 1, "bento" to 0), pantry.stock.map { it.food.code to it.quantity })
        assertEquals(3, pantry.feedsLeftToday)
    }

    @Test
    fun `buy spends the cost and adds the food`() {
        coinRepository.record(user.id, 100, CoinReason.ACTIVITY, null)

        val pantry = service.buy(user.id, "banana")

        assertEquals(50, pantry.coinBalance)
        assertEquals(1, pantry.stock.first().quantity)
        assertEquals(CoinReason.FOOD, coinRepository.transactions.last().reason)
    }

    @Test
    fun `buy without enough TomoCoins fails with the shortfall`() {
        coinRepository.record(user.id, 100, CoinReason.ACTIVITY, null)

        val e = assertThrows<NotEnoughCoinsException> { service.buy(user.id, "bento") }

        assertEquals(50, e.shortfall)
        assertEquals(500, e.distanceNeededM)
        assertEquals(100, userRepository.findById(user.id)!!.coinBalance)
    }

    @Test
    fun `feed uses one food and adds its Bond`() {
        inventory.add(user.id, "bento")
        tomoRepository.updateProgress(tomo.id, 0, bondPoints = 45, bondLevel = 1)

        val result = service.feed(user.id, tomo.id, "bento")

        assertEquals(55, result.tomo.bondPoints)
        assertEquals(1, result.bondLevelBefore)
        assertEquals(2, result.tomo.bondLevel)
        assertEquals(0, result.pantry.stock.last().quantity)
        assertEquals(2, result.pantry.feedsLeftToday)
    }

    @Test
    fun `a full Tomo gets no Bond and the Player keeps the food`() {
        repeat(4) { inventory.add(user.id, "banana") }
        repeat(3) { service.feed(user.id, tomo.id, "banana") }

        assertThrows<FeedingLimitException> { service.feed(user.id, tomo.id, "banana") }

        assertEquals(1, inventory.findAll(user.id)["banana"])
        assertEquals(9, tomoRepository.findById(tomo.id)!!.bondPoints)
    }

    @Test
    fun `feed without the food or for another Player's Tomo fails`() {
        assertThrows<NoFoodLeftException> { service.feed(user.id, tomo.id, "banana") }

        val other = userRepository.create("sub-2", "Runner0002", "00000002")!!
        inventory.add(other.id, "banana")
        assertThrows<TomoNotFoundException> { service.feed(other.id, tomo.id, "banana") }
        assertThrows<FoodNotFoundException> { service.feed(user.id, tomo.id, "cake") }
    }
}

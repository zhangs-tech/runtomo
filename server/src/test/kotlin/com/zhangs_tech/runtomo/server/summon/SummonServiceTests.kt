package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.coin.NotEnoughCoinsException
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class SummonServiceTests {
    private val userRepository = InMemoryUserRepository()
    private val tomoRepository = InMemoryTomoRepository()
    private val coinRepository = InMemoryCoinRepository(userRepository)
    private val summonRepository = InMemorySummonRepository()
    private val pityRepository = InMemoryPityRepository()
    private val user = userRepository.create("sub-1", "Runner0001", "00000001")!!

    private fun service(vararg rolls: Int) = SummonService(
        testGameData,
        GameCalendar(testDaytimeClock),
        SummonRoller(FixedRandom(*rolls)),
        RewardRules(testGameData),
        userRepository,
        tomoRepository,
        coinRepository,
        summonRepository,
        pityRepository,
    )

    private fun giveCoins(amount: Int) = coinRepository.record(user.id, amount, CoinReason.ACTIVITY, null)

    @Test
    fun `summon spends the cost and creates a new Tomo`() {
        giveCoins(1500)

        val result = service(0).summon(user.id, "standard") // roll 0 picks luppy

        assertEquals("luppy", result.species.code)
        assertEquals("Luppy", result.tomo!!.nickname)
        assertEquals(LocalDate.of(2026, 11, 14), result.tomo.birthday)
        assertEquals(500, result.coinBalance)
        assertFalse(result.summon.wasDuplicate)
        assertEquals(result.summon.id, coinRepository.transactions.last().refId)
        assertEquals(1, pityRepository.pullsSinceEpic(user.id, "standard"))
    }

    @Test
    fun `a duplicate converts to TomoCoins`() {
        giveCoins(1000)
        tomoRepository.create(user.id, "luppy", "Mochi", LocalDate.of(2026, 11, 1))

        val result = service(0).summon(user.id, "standard")

        assertTrue(result.summon.wasDuplicate)
        assertNull(result.tomo)
        assertEquals(200, result.duplicateCoins)
        assertEquals(200, result.coinBalance)
        assertEquals(listOf(CoinReason.SUMMON, CoinReason.DUPLICATE), coinRepository.transactions.drop(1).map { it.reason })
        assertEquals(1, tomoRepository.tomos.size)
    }

    @Test
    fun `not enough TomoCoins fails with the shortfall and spends nothing`() {
        giveCoins(700)

        val e = assertThrows<NotEnoughCoinsException> { service(0).summon(user.id, "standard") }

        assertEquals(300, e.shortfall)
        assertEquals(3000, e.distanceNeededM)
        assertEquals(700, userRepository.findById(user.id)!!.coinBalance)
        assertTrue(summonRepository.summons.isEmpty())
    }

    @Test
    fun `the roll uses the total weight of the banner`() {
        giveCoins(1000)
        val random = FixedRandom(950)

        val result = SummonService(
            testGameData, GameCalendar(testDaytimeClock), SummonRoller(random), RewardRules(testGameData),
            userRepository, tomoRepository, coinRepository, summonRepository, pityRepository,
        ).summon(user.id, "standard")

        assertEquals(listOf(1000), random.bounds)
        assertEquals("nova", result.species.code)
        assertEquals(0, pityRepository.pullsSinceEpic(user.id, "standard"))
    }

    @Test
    fun `pity gives Epic or higher after the threshold`() {
        giveCoins(3000)
        pityRepository.save(user.id, "standard", 2) // threshold 3: the next summon is pity

        val result = service(0).summon(user.id, "standard")

        assertTrue(result.summon.wasPity)
        assertEquals("nova", result.species.code)
        assertEquals(0, pityRepository.pullsSinceEpic(user.id, "standard"))
    }

    @Test
    fun `a pool without Epic species has no pity counter`() {
        val view = service().bannerView(user.id, "commons")

        assertNull(view.pityRemaining)
    }

    @Test
    fun `banner view shows the balance and summons left until pity`() {
        giveCoins(1200)
        pityRepository.save(user.id, "standard", 1)

        val view = service().bannerView(user.id, "standard")

        assertEquals(1200, view.coinBalance)
        assertEquals(2, view.pityRemaining)
    }

    @Test
    fun `latest returns the newest summon with the current balance`() {
        giveCoins(2000)
        val service = service(0, 950)
        service.summon(user.id, "standard")
        service.summon(user.id, "standard")

        val latest = service.latest(user.id, 1).single()

        assertEquals("nova", latest.species.code)
        assertNotNull(latest.tomo)
        assertEquals(0, latest.coinBalance)
    }

    @Test
    fun `an unknown banner is refused`() {
        assertThrows<BannerNotFoundException> { service().summon(user.id, "unknown") }
    }
}

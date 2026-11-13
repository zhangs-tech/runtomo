package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
import com.zhangs_tech.runtomo.server.tomo.Mood
import com.zhangs_tech.runtomo.server.tomo.MoodRules
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class ActivityServiceTests {
    private val calendar = GameCalendar(testDaytimeClock)
    private val today = LocalDate.of(2026, 11, 14)
    private val userRepository = InMemoryUserRepository()
    private val tomoRepository = InMemoryTomoRepository()
    private val dailyStatsRepository = InMemoryDailyStatsRepository()
    private val coinRepository = InMemoryCoinRepository(userRepository)
    private val service = ActivityService(
        gameData = testGameData,
        gameCalendar = calendar,
        rewardRules = RewardRules(testGameData),
        moodRules = MoodRules(testGameData),
        dailyStatsRepository = dailyStatsRepository,
        userRepository = userRepository,
        tomoRepository = tomoRepository,
        coinRepository = coinRepository,
    )

    private val user = userRepository.create("sub-1", "Runner0001", "00000001")!!
    private val tomo = tomoRepository.create(user.id, "luppy", "Mochi", today).also {
        userRepository.setEquippedTomo(user.id, it.id)
    }

    @Test
    fun `first sync pays coins and Bond for the whole distance`() {
        val result = service.sync(user.id, today, steps = 1600) // 1200 m

        val rewards = result.rewards!!
        assertEquals(1200, rewards.distanceM)
        assertEquals(120, rewards.coins)
        assertEquals(12, rewards.bond)
        assertEquals("Mochi", rewards.tomoNickname)
        assertEquals(1200, rewards.distanceTogetherM)
        assertEquals(120, result.coinBalance)
        assertEquals(Mood.HAPPY, result.mood)
        assertEquals(listOf("walk-1km"), rewards.completedQuests.map { it.code })
        assertTrue(rewards.showRewardScreen)
        assertEquals(CoinReason.ACTIVITY, coinRepository.transactions.single().reason)
    }

    @Test
    fun `second sync pays only the new part`() {
        service.sync(user.id, today, steps = 1600) // 1200 m

        val rewards = service.sync(user.id, today, steps = 2000).rewards!! // 1500 m

        assertEquals(300, rewards.distanceM)
        assertEquals(30, rewards.coins)
        assertEquals(3, rewards.bond)
        assertFalse(rewards.showRewardScreen)
        assertEquals(150, userRepository.findById(user.id)!!.coinBalance)
        assertEquals(1500, tomoRepository.findById(tomo.id)!!.distanceTogetherM)
    }

    @Test
    fun `the same or a lower total pays nothing and keeps the higher total`() {
        service.sync(user.id, today, steps = 2000)

        val same = service.sync(user.id, today, steps = 2000)
        val lower = service.sync(user.id, today, steps = 100)

        assertNull(same.rewards)
        assertNull(lower.rewards)
        assertEquals(2000, lower.stats.steps)
        assertEquals(1, coinRepository.transactions.size)
    }

    @Test
    fun `past the coin cap the sync still pays Bond and Distance Together`() {
        service.sync(user.id, today, steps = 14_000) // 10500 m

        val rewards = service.sync(user.id, today, steps = 16_000).rewards!! // 12000 m

        assertEquals(0, rewards.coins)
        assertEquals(15, rewards.bond)
        assertTrue(rewards.coinCapReached)
        assertEquals(750, userRepository.findById(user.id)!!.coinBalance)
    }

    @Test
    fun `crossing a Bond threshold raises the Bond Level`() {
        val rewards = service.sync(user.id, today, steps = 7000).rewards!! // 5250 m, 52 Bond

        assertEquals(1, rewards.bondLevelBefore)
        assertEquals(2, rewards.bondLevelAfter)
        assertEquals(2, tomoRepository.findById(tomo.id)!!.bondLevel)
    }

    @Test
    fun `a sync for another day is refused`() {
        assertThrows<DayClosedException> { service.sync(user.id, today.minusDays(1), steps = 100) }
        assertThrows<DayClosedException> { service.sync(user.id, today.plusDays(1), steps = 100) }
    }

    @Test
    fun `steps out of range are refused`() {
        assertThrows<InvalidStepsException> { service.sync(user.id, today, steps = -1) }
        assertThrows<InvalidStepsException> { service.sync(user.id, today, steps = 100_001) }
    }

    @Test
    fun `mood uses yesterday when today is quiet`() {
        dailyStatsRepository.save(DailyStats(user.id, today.minusDays(1), steps = 2000, distanceM = 1500))

        assertEquals(Mood.RELAXED, service.currentMood(user.id, todayDistanceM = 0))
    }
}

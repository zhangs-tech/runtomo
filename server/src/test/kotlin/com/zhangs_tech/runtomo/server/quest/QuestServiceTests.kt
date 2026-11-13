package com.zhangs_tech.runtomo.server.quest

import com.zhangs_tech.runtomo.server.activity.DailyStats
import com.zhangs_tech.runtomo.server.activity.InMemoryDailyStatsRepository
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class QuestServiceTests {
    private val today = LocalDate.of(2026, 11, 14)
    private val userRepository = InMemoryUserRepository()
    private val claimRepository = InMemoryQuestClaimRepository()
    private val dailyStatsRepository = InMemoryDailyStatsRepository()
    private val service = QuestService(
        testGameData,
        GameCalendar(testDaytimeClock),
        dailyStatsRepository,
        claimRepository,
        InMemoryCoinRepository(userRepository),
    )
    private val user = userRepository.create("sub-1", "Runner0001", "00000001")!!

    @Test
    fun `today quests show progress and status`() {
        dailyStatsRepository.save(DailyStats(user.id, today, steps = 2000, distanceM = 1500))

        val quests = service.todayQuests(user.id).associateBy { it.quest.code }

        assertEquals(QuestStatus.CLAIMABLE, quests.getValue("walk-1km").status)
        assertEquals(1500, quests.getValue("walk-3km").progress)
        assertEquals(QuestStatus.IN_PROGRESS, quests.getValue("walk-3km").status)
        assertEquals(2000, quests.getValue("steps-5000").progress)
    }

    @Test
    fun `claim credits the reward once`() {
        dailyStatsRepository.save(DailyStats(user.id, today, steps = 2000, distanceM = 1500))

        assertEquals(100, service.claim(user.id, "walk-1km"))
        assertThrows<QuestAlreadyClaimedException> { service.claim(user.id, "walk-1km") }
        assertEquals(QuestStatus.CLAIMED, service.todayQuests(user.id).first().status)
    }

    @Test
    fun `claim refuses a quest that is not complete or does not exist`() {
        assertThrows<QuestNotCompleteException> { service.claim(user.id, "walk-1km") }
        assertThrows<QuestNotFoundException> { service.claim(user.id, "unknown") }
    }

    @Test
    fun `a completed quest from an earlier day is not paid`() {
        dailyStatsRepository.save(DailyStats(user.id, today.minusDays(1), steps = 6000, distanceM = 4500))

        val quests = service.todayQuests(user.id)

        assertEquals(List(3) { QuestStatus.IN_PROGRESS }, quests.map { it.status })
        assertEquals(emptyList<InMemoryQuestClaimRepository.Claim>(), claimRepository.claims)
        assertEquals(0, userRepository.findById(user.id)!!.coinBalance)
    }
}

package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.quest.InMemoryQuestClaimRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class HistoryServiceTests {
    private val userId = UUID.randomUUID()
    private val dailyStatsRepository = InMemoryDailyStatsRepository()
    private val claimRepository = InMemoryQuestClaimRepository()
    private val service = HistoryService(testGameData, GameCalendar(testDaytimeClock), dailyStatsRepository, claimRepository)

    // Today is Saturday 2026-11-14. The week started on Monday 2026-11-09.
    private fun day(date: String, steps: Int, coins: Int = 0, bond: Int = 0) =
        dailyStatsRepository.save(DailyStats(userId, LocalDate.parse(date), steps, steps * 3 / 4, coins, bond))

    @Test
    fun `totals use the Monday week and the calendar month`() {
        day("2026-11-14", 1000)
        day("2026-11-09", 2000)
        day("2026-11-08", 4000) // Sunday of the week before
        day("2026-10-31", 8000) // last month

        val totals = service.totals(userId)

        assertEquals(ActivityTotal(3000, 2250), totals.week)
        assertEquals(ActivityTotal(7000, 5250), totals.month)
    }

    @Test
    fun `days are newest first, include claimed quest coins, and page with a cursor`() {
        day("2026-11-14", 1000, coins = 75, bond = 7)
        day("2026-11-13", 2000)
        day("2026-11-12", 3000)
        claimRepository.create(userId, "walk-1km", LocalDate.parse("2026-11-14"))

        val first = service.days(userId, cursor = null, limit = 2)
        val second = service.days(userId, cursor = first.nextCursor, limit = 2)

        assertEquals(listOf("2026-11-14", "2026-11-13"), first.days.map { it.date.toString() })
        assertEquals(175, first.days[0].coins)
        assertEquals(7, first.days[0].bond)
        assertEquals(LocalDate.parse("2026-11-13"), first.nextCursor)
        assertEquals(listOf("2026-11-12"), second.days.map { it.date.toString() })
        assertNull(second.nextCursor)
    }
}

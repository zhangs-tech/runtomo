package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.quest.QuestClaimRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

data class PeriodTotals(val week: ActivityTotal, val month: ActivityTotal)

/** One synced day in the history. [coins] includes the quest rewards claimed that day. */
data class HistoryDay(val date: LocalDate, val steps: Int, val distanceM: Int, val coins: Int, val bond: Int)

data class HistoryPage(val days: List<HistoryDay>, val nextCursor: LocalDate?)

/** UC-07 View Daily History. */
@Service
class HistoryService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val dailyStatsRepository: DailyStatsRepository,
    private val questClaimRepository: QuestClaimRepository,
) {
    /** BR-2: weeks run Monday to Sunday, and months by calendar month, in UTC+7. */
    fun totals(userId: UUID): PeriodTotals {
        val today = gameCalendar.today()
        return PeriodTotals(
            week = dailyStatsRepository.sum(userId, gameCalendar.startOfWeek(today), today),
            month = dailyStatsRepository.sum(userId, gameCalendar.startOfMonth(today), today),
        )
    }

    fun days(userId: UUID, cursor: LocalDate?, limit: Int): HistoryPage {
        val pageSize = limit.coerceIn(1, MAX_PAGE_SIZE)
        val stats = dailyStatsRepository.findPage(userId, cursor, pageSize)
        val claims = questClaimRepository.findClaimedCodes(userId, stats.map { it.localDate })
        val days = stats.map { day ->
            val questCoins = claims[day.localDate].orEmpty().sumOf { gameData.findQuest(it)?.reward ?: 0 }
            HistoryDay(day.localDate, day.steps, day.distanceM, day.activityCoins + questCoins, day.bondPoints)
        }
        return HistoryPage(days, nextCursor = days.lastOrNull()?.date?.takeIf { days.size == pageSize })
    }

    private companion object {
        const val MAX_PAGE_SIZE = 60
    }
}

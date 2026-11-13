package com.zhangs_tech.runtomo.server.activity

import java.time.LocalDate
import java.util.UUID

class InMemoryDailyStatsRepository : DailyStatsRepository {
    val days = mutableMapOf<Pair<UUID, LocalDate>, DailyStats>()

    override fun find(userId: UUID, localDate: LocalDate): DailyStats? = days[userId to localDate]

    override fun lockOrCreate(userId: UUID, localDate: LocalDate): DailyStats =
        days.getOrPut(userId to localDate) { DailyStats(userId, localDate) }

    override fun save(stats: DailyStats) {
        days[stats.userId to stats.localDate] = stats
    }

    override fun findLatestDate(userId: UUID): LocalDate? =
        days.keys.filter { it.first == userId }.maxOfOrNull { it.second }
}

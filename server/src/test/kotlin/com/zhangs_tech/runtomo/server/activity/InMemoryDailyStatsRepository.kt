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

    override fun sum(userId: UUID, from: LocalDate, to: LocalDate): ActivityTotal {
        val inRange = days.values.filter { it.userId == userId && it.localDate in from..to }
        return ActivityTotal(inRange.sumOf { it.steps.toLong() }, inRange.sumOf { it.distanceM.toLong() })
    }

    override fun sumDistanceByUser(userIds: Collection<UUID>, from: LocalDate, to: LocalDate): Map<UUID, Long> =
        days.values
            .filter { it.userId in userIds && it.localDate in from..to }
            .groupBy { it.userId }
            .mapValues { (_, days) -> days.sumOf { it.distanceM.toLong() } }

    override fun findPage(userId: UUID, before: LocalDate?, limit: Int): List<DailyStats> =
        days.values
            .filter { it.userId == userId && (before == null || it.localDate < before) }
            .sortedByDescending { it.localDate }
            .take(limit)

    override fun findLatestDate(userId: UUID): LocalDate? =
        days.keys.filter { it.first == userId }.maxOfOrNull { it.second }
}

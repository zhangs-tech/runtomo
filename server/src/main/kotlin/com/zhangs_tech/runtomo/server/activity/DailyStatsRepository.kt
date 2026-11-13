package com.zhangs_tech.runtomo.server.activity

import java.time.LocalDate
import java.util.UUID

interface DailyStatsRepository {
    fun find(userId: UUID, localDate: LocalDate): DailyStats?

    /** Creates the row if it does not exist, and locks it until the transaction ends. */
    fun lockOrCreate(userId: UUID, localDate: LocalDate): DailyStats

    /** Saves the totals and sets `synced_at` to now. */
    fun save(stats: DailyStats)

    fun findLatestDate(userId: UUID): LocalDate?

    /** Sums steps and distance from [from] to [to], both included. */
    fun sum(userId: UUID, from: LocalDate, to: LocalDate): ActivityTotal

    /** Distance from [from] to [to], both included, for each of [userIds]. Users without days are missing. */
    fun sumDistanceByUser(userIds: Collection<UUID>, from: LocalDate, to: LocalDate): Map<UUID, Long>

    /** Up to [limit] days before [before] (or all days if null), newest first. */
    fun findPage(userId: UUID, before: LocalDate?, limit: Int): List<DailyStats>

}

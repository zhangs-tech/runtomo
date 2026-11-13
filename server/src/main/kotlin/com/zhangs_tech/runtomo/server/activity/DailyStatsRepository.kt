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

}

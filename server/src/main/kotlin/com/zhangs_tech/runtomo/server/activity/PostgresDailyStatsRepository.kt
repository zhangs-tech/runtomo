package com.zhangs_tech.runtomo.server.activity

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class PostgresDailyStatsRepository(private val jdbcClient: JdbcClient) : DailyStatsRepository {
    override fun find(userId: UUID, localDate: LocalDate): DailyStats? =
        jdbcClient
            .sql("SELECT $COLUMNS FROM daily_stats WHERE user_id = :userId AND local_date = :localDate")
            .param("userId", userId)
            .param("localDate", localDate)
            .query(ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun lockOrCreate(userId: UUID, localDate: LocalDate): DailyStats {
        jdbcClient
            .sql(
                """
                INSERT INTO daily_stats (user_id, local_date) VALUES (:userId, :localDate)
                ON CONFLICT (user_id, local_date) DO NOTHING
                """.trimIndent()
            )
            .param("userId", userId)
            .param("localDate", localDate)
            .update()
        return jdbcClient
            .sql("SELECT $COLUMNS FROM daily_stats WHERE user_id = :userId AND local_date = :localDate FOR UPDATE")
            .param("userId", userId)
            .param("localDate", localDate)
            .query(ROW_MAPPER)
            .single()
    }

    override fun save(stats: DailyStats) {
        jdbcClient
            .sql(
                """
                UPDATE daily_stats
                SET steps = :steps, distance_m = :distanceM, activity_coins = :activityCoins,
                    bond_points = :bondPoints, synced_at = now()
                WHERE user_id = :userId AND local_date = :localDate
                """.trimIndent()
            )
            .param("userId", stats.userId)
            .param("localDate", stats.localDate)
            .param("steps", stats.steps)
            .param("distanceM", stats.distanceM)
            .param("activityCoins", stats.activityCoins)
            .param("bondPoints", stats.bondPoints)
            .update()
    }

    override fun findLatestDate(userId: UUID): LocalDate? =
        jdbcClient
            .sql("SELECT max(local_date) FROM daily_stats WHERE user_id = :userId")
            .param("userId", userId)
            .query(LocalDate::class.java)
            .optional()
            .orElse(null)

    private companion object {
        const val COLUMNS = "user_id, local_date, steps, distance_m, activity_coins, bond_points"

        val ROW_MAPPER = RowMapper { rs, _ ->
            DailyStats(
                userId = rs.getObject("user_id", UUID::class.java),
                localDate = rs.getObject("local_date", LocalDate::class.java),
                steps = rs.getInt("steps"),
                distanceM = rs.getInt("distance_m"),
                activityCoins = rs.getInt("activity_coins"),
                bondPoints = rs.getInt("bond_points"),
            )
        }
    }
}

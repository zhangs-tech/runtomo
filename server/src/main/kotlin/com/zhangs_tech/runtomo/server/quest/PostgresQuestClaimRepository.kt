package com.zhangs_tech.runtomo.server.quest

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class PostgresQuestClaimRepository(private val jdbcClient: JdbcClient) : QuestClaimRepository {
    override fun findClaimedCodes(userId: UUID, localDate: LocalDate): Set<String> =
        jdbcClient
            .sql("SELECT quest_code FROM quest_claims WHERE user_id = :userId AND local_date = :localDate")
            .param("userId", userId)
            .param("localDate", localDate)
            .query(String::class.java)
            .list()
            .filterNotNull()
            .toSet()

    override fun findClaimedCodes(userId: UUID, dates: Collection<LocalDate>): Map<LocalDate, Set<String>> {
        if (dates.isEmpty()) return emptyMap()
        return jdbcClient
            .sql("SELECT local_date, quest_code FROM quest_claims WHERE user_id = :userId AND local_date IN (:dates)")
            .param("userId", userId)
            .param("dates", dates)
            .query { rs, _ -> rs.getObject("local_date", LocalDate::class.java) to rs.getString("quest_code") }
            .list()
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }
    }

    override fun create(userId: UUID, questCode: String, localDate: LocalDate): UUID? =
        jdbcClient
            .sql(
                """
                INSERT INTO quest_claims (user_id, quest_code, local_date)
                VALUES (:userId, :questCode, :localDate)
                ON CONFLICT (user_id, local_date, quest_code) DO NOTHING
                RETURNING id
                """.trimIndent()
            )
            .param("userId", userId)
            .param("questCode", questCode)
            .param("localDate", localDate)
            .query(UUID::class.java)
            .optional()
            .orElse(null)
}

package com.zhangs_tech.runtomo.server.summon

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresPityRepository(private val jdbcClient: JdbcClient) : PityRepository {
    override fun pullsSinceEpic(userId: UUID, bannerCode: String): Int =
        jdbcClient
            .sql("SELECT pulls_since_epic FROM user_pity WHERE user_id = :userId AND banner_code = :bannerCode")
            .param("userId", userId)
            .param("bannerCode", bannerCode)
            .query(Int::class.java)
            .optional()
            .orElse(0)

    override fun save(userId: UUID, bannerCode: String, pullsSinceEpic: Int) {
        jdbcClient
            .sql(
                """
                INSERT INTO user_pity (user_id, banner_code, pulls_since_epic)
                VALUES (:userId, :bannerCode, :pulls)
                ON CONFLICT (user_id, banner_code) DO UPDATE SET pulls_since_epic = EXCLUDED.pulls_since_epic
                """.trimIndent()
            )
            .param("userId", userId)
            .param("bannerCode", bannerCode)
            .param("pulls", pullsSinceEpic)
            .update()
    }
}

package com.zhangs_tech.runtomo.server.coin

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresCoinRepository(private val jdbcClient: JdbcClient) : CoinRepository {
    override fun record(userId: UUID, amount: Int, reason: CoinReason, refId: UUID?): Long {
        jdbcClient
            .sql(
                """
                INSERT INTO coin_transactions (user_id, amount, reason, ref_id)
                VALUES (:userId, :amount, :reason, :refId)
                """.trimIndent()
            )
            .param("userId", userId)
            .param("amount", amount)
            .param("reason", reason.dbValue)
            .param("refId", refId)
            .update()
        return jdbcClient
            .sql("UPDATE users SET coin_balance = coin_balance + :amount WHERE id = :userId RETURNING coin_balance")
            .param("userId", userId)
            .param("amount", amount)
            .query(Long::class.java)
            .single()
    }
}

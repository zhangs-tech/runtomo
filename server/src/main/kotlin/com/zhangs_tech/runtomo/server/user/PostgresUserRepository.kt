package com.zhangs_tech.runtomo.server.user

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresUserRepository(private val jdbcClient: JdbcClient) : UserRepository {


    override fun findById(id: UUID): User? =
        jdbcClient
            .sql("SELECT $USER_COLUMNS FROM users WHERE id = :id")
            .param("id", id)
            .query(USER_ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun findByAuthSubject(authSubject: String): User? =
        jdbcClient
            .sql("SELECT $USER_COLUMNS FROM users WHERE auth_subject = :authSubject")
            .param("authSubject", authSubject)
            .query(USER_ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun create(authSubject: String, displayName: String, friendCode: String): User? =
        jdbcClient
            .sql(
                """
                INSERT INTO users (auth_subject, display_name, friend_code)
                VALUES (:authSubject, :displayName, :friendCode)
                ON CONFLICT (friend_code) DO NOTHING
                RETURNING $USER_COLUMNS
                """.trimIndent()
            )
            .param("authSubject", authSubject)
            .param("displayName", displayName)
            .param("friendCode", friendCode)
            .query(USER_ROW_MAPPER)
            .optional()
            .orElse(null)

    private companion object {
        const val USER_COLUMNS =
            "id, auth_subject, display_name, friend_code, coin_balance, equipped_tomo_id, created_at"

        val USER_ROW_MAPPER = RowMapper { rs, _ ->
            User(
                id = rs.getObject("id", UUID::class.java),
                authSubject = rs.getString("auth_subject"),
                displayName = rs.getString("display_name"),
                friendCode = rs.getString("friend_code"),
                coinBalance = rs.getLong("coin_balance"),
                equippedTomoId = rs.getObject("equipped_tomo_id", UUID::class.java),
                createdAt = rs.getTimestamp("created_at").toInstant()
            )
        }
    }
}

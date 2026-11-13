package com.zhangs_tech.runtomo.server.auth

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Repository
class PostgresRefreshTokenRepository(private val jdbcClient: JdbcClient) : RefreshTokenRepository {
    override fun findByTokenHash(tokenHash: String): RefreshToken? =
        jdbcClient
            .sql("SELECT $REFRESH_TOKEN_COLUMNS FROM refresh_tokens WHERE token_hash = :tokenHash")
            .param("tokenHash", tokenHash)
            .query(REFRESH_TOKEN_ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun createFamily(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken =
        jdbcClient
            .sql(
                """
                INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, expires_at)
                SELECT new_id, :userId, new_id, :tokenHash, :expiresAt
                FROM (SELECT uuidv7() AS new_id) AS generated
                RETURNING $REFRESH_TOKEN_COLUMNS
                """.trimIndent()
            )
            .param("userId", userId)
            .param("tokenHash", tokenHash)
            .param("expiresAt", Timestamp.from(expiresAt))
            .query(REFRESH_TOKEN_ROW_MAPPER)
            .single()

    override fun create(
        userId: UUID,
        familyId: UUID,
        tokenHash: String,
        expiresAt: Instant
    ): RefreshToken =
        jdbcClient
            .sql(
                """
                INSERT INTO refresh_tokens (user_id, family_id, token_hash, expires_at)
                VALUES (:userId, :familyId, :tokenHash, :expiresAt)
                RETURNING $REFRESH_TOKEN_COLUMNS
                """.trimIndent()
            )
            .param("userId", userId)
            .param("familyId", familyId)
            .param("tokenHash", tokenHash)
            .param("expiresAt", Timestamp.from(expiresAt))
            .query(REFRESH_TOKEN_ROW_MAPPER)
            .single()

    override fun revoke(id: UUID): Boolean =
        jdbcClient
            .sql("UPDATE refresh_tokens SET revoked_at = now() WHERE id = :id AND revoked_at IS NULL")
            .param("id", id)
            .update() == 1

    override fun revokeFamily(familyId: UUID) {
        jdbcClient
            .sql("UPDATE refresh_tokens SET revoked_at = now() WHERE family_id = :familyId AND revoked_at IS NULL")
            .param("familyId", familyId)
            .update()
    }

    private companion object {
        const val REFRESH_TOKEN_COLUMNS =
            "id, user_id, family_id, token_hash, expires_at, revoked_at, created_at"

        val REFRESH_TOKEN_ROW_MAPPER = RowMapper<RefreshToken> { rs, _ ->
            RefreshToken(
                id = rs.getObject("id", UUID::class.java),
                userId = rs.getObject("user_id", UUID::class.java),
                familyId = rs.getObject("family_id", UUID::class.java),
                tokenHash = rs.getString("token_hash"),
                expiresAt = rs.getTimestamp("expires_at").toInstant(),
                revokedAt = rs.getTimestamp("revoked_at")?.toInstant(),
                createdAt = rs.getTimestamp("created_at").toInstant()
            )
        }
    }
}

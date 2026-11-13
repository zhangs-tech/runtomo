package com.zhangs_tech.runtomo.server.social

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

/** One row per pair, with the smaller user ID in `user_a_id` (docs/erd.md). */
@Repository
class PostgresFriendRepository(private val jdbcClient: JdbcClient) : FriendRepository {
    override fun areFriends(userId: UUID, otherUserId: UUID): Boolean {
        val (a, b) = ordered(userId, otherUserId)
        return jdbcClient
            .sql("SELECT count(*) FROM friendships WHERE user_a_id = :a AND user_b_id = :b")
            .param("a", a)
            .param("b", b)
            .query(Int::class.java)
            .single() > 0
    }

    override fun count(userId: UUID): Int =
        jdbcClient
            .sql("SELECT count(*) FROM friendships WHERE user_a_id = :userId OR user_b_id = :userId")
            .param("userId", userId)
            .query(Int::class.java)
            .single()

    override fun findFriendIds(userId: UUID): List<UUID> =
        jdbcClient
            .sql(
                """
                SELECT CASE WHEN user_a_id = :userId THEN user_b_id ELSE user_a_id END AS friend_id
                FROM friendships WHERE user_a_id = :userId OR user_b_id = :userId
                """.trimIndent()
            )
            .param("userId", userId)
            .query { rs, _ -> rs.getObject("friend_id", UUID::class.java) }
            .list()

    override fun create(userId: UUID, otherUserId: UUID) {
        val (a, b) = ordered(userId, otherUserId)
        jdbcClient
            .sql("INSERT INTO friendships (user_a_id, user_b_id) VALUES (:a, :b) ON CONFLICT DO NOTHING")
            .param("a", a)
            .param("b", b)
            .update()
    }

    override fun delete(userId: UUID, otherUserId: UUID) {
        val (a, b) = ordered(userId, otherUserId)
        jdbcClient
            .sql("DELETE FROM friendships WHERE user_a_id = :a AND user_b_id = :b")
            .param("a", a)
            .param("b", b)
            .update()
    }

    // PostgreSQL compares UUIDs as unsigned bytes, the same order as their text form.
    private fun ordered(x: UUID, y: UUID): Pair<UUID, UUID> = if (x.toString() < y.toString()) x to y else y to x
}

@Repository
class PostgresFriendRequestRepository(private val jdbcClient: JdbcClient) : FriendRequestRepository {
    override fun findById(id: UUID): FriendRequest? =
        jdbcClient
            .sql("SELECT $COLUMNS FROM friend_requests WHERE id = :id")
            .param("id", id)
            .query(ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun find(fromUserId: UUID, toUserId: UUID): FriendRequest? =
        jdbcClient
            .sql("SELECT $COLUMNS FROM friend_requests WHERE from_user_id = :fromUserId AND to_user_id = :toUserId")
            .param("fromUserId", fromUserId)
            .param("toUserId", toUserId)
            .query(ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun findIncoming(userId: UUID): List<FriendRequest> =
        jdbcClient
            .sql("SELECT $COLUMNS FROM friend_requests WHERE to_user_id = :userId ORDER BY created_at")
            .param("userId", userId)
            .query(ROW_MAPPER)
            .list()

    override fun create(fromUserId: UUID, toUserId: UUID) {
        jdbcClient
            .sql(
                """
                INSERT INTO friend_requests (from_user_id, to_user_id) VALUES (:fromUserId, :toUserId)
                ON CONFLICT (from_user_id, to_user_id) DO NOTHING
                """.trimIndent()
            )
            .param("fromUserId", fromUserId)
            .param("toUserId", toUserId)
            .update()
    }

    override fun delete(id: UUID) {
        jdbcClient.sql("DELETE FROM friend_requests WHERE id = :id").param("id", id).update()
    }

    private companion object {
        const val COLUMNS = "id, from_user_id, to_user_id, created_at"

        val ROW_MAPPER = RowMapper { rs, _ ->
            FriendRequest(
                id = rs.getObject("id", UUID::class.java),
                fromUserId = rs.getObject("from_user_id", UUID::class.java),
                toUserId = rs.getObject("to_user_id", UUID::class.java),
                createdAt = rs.getTimestamp("created_at").toInstant(),
            )
        }
    }
}

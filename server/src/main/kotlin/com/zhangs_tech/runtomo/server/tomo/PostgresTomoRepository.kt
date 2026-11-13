package com.zhangs_tech.runtomo.server.tomo

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class PostgresTomoRepository(private val jdbcClient: JdbcClient) : TomoRepository {
    override fun findByUserId(userId: UUID): List<Tomo> =
        jdbcClient
            .sql("SELECT $TOMO_COLUMNS FROM user_tomos WHERE user_id = :userId ORDER BY birthday, id")
            .param("userId", userId)
            .query(TOMO_ROW_MAPPER)
            .list()

    override fun findById(id: UUID): Tomo? =
        jdbcClient
            .sql("SELECT $TOMO_COLUMNS FROM user_tomos WHERE id = :id")
            .param("id", id)
            .query(TOMO_ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun updateProgress(id: UUID, distanceTogetherM: Long, bondPoints: Int, bondLevel: Int) {
        jdbcClient
            .sql(
                """
                UPDATE user_tomos
                SET distance_together_m = :distanceTogetherM, bond_points = :bondPoints, bond_level = :bondLevel
                WHERE id = :id
                """.trimIndent()
            )
            .param("id", id)
            .param("distanceTogetherM", distanceTogetherM)
            .param("bondPoints", bondPoints)
            .param("bondLevel", bondLevel)
            .update()
    }

    override fun rename(id: UUID, userId: UUID, nickname: String): Tomo? =
        jdbcClient
            .sql("UPDATE user_tomos SET nickname = :nickname WHERE id = :id AND user_id = :userId RETURNING $TOMO_COLUMNS")
            .param("id", id)
            .param("userId", userId)
            .param("nickname", nickname)
            .query(TOMO_ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun create(userId: UUID, speciesCode: String, nickname: String, birthday: LocalDate): Tomo =
        jdbcClient
            .sql(
                """
                INSERT INTO user_tomos (user_id, species_code, nickname, birthday)
                VALUES (:userId, :speciesCode, :nickname, :birthday)
                RETURNING $TOMO_COLUMNS
                """.trimIndent()
            )
            .param("userId", userId)
            .param("speciesCode", speciesCode)
            .param("nickname", nickname)
            .param("birthday", birthday)
            .query(TOMO_ROW_MAPPER)
            .single()

    private companion object {
        const val TOMO_COLUMNS =
            "id, user_id, species_code, nickname, bond_points, bond_level, distance_together_m, birthday"

        val TOMO_ROW_MAPPER = RowMapper { rs, _ ->
            Tomo(
                id = rs.getObject("id", UUID::class.java),
                userId = rs.getObject("user_id", UUID::class.java),
                speciesCode = rs.getString("species_code"),
                nickname = rs.getString("nickname"),
                bondPoints = rs.getInt("bond_points"),
                bondLevel = rs.getInt("bond_level"),
                distanceTogetherM = rs.getLong("distance_together_m"),
                birthday = rs.getObject("birthday", LocalDate::class.java),
            )
        }
    }
}

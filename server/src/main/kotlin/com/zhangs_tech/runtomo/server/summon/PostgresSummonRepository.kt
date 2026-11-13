package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.game.Rarity
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostgresSummonRepository(private val jdbcClient: JdbcClient) : SummonRepository {
    override fun create(
        userId: UUID,
        bannerCode: String,
        speciesCode: String,
        rarity: Rarity,
        costCoins: Int,
        wasPity: Boolean,
        userTomoId: UUID?,
    ): Summon =
        jdbcClient
            .sql(
                """
                INSERT INTO summons
                    (user_id, banner_code, species_code, rarity, cost_coins, was_pity, was_duplicate, user_tomo_id)
                VALUES (:userId, :bannerCode, :speciesCode, :rarity, :costCoins, :wasPity, :wasDuplicate, :userTomoId)
                RETURNING $COLUMNS
                """.trimIndent()
            )
            .param("userId", userId)
            .param("bannerCode", bannerCode)
            .param("speciesCode", speciesCode)
            .param("rarity", rarity.name)
            .param("costCoins", costCoins)
            .param("wasPity", wasPity)
            .param("wasDuplicate", userTomoId == null)
            .param("userTomoId", userTomoId)
            .query(ROW_MAPPER)
            .single()

    override fun findLatest(userId: UUID, limit: Int): List<Summon> =
        jdbcClient
            .sql("SELECT $COLUMNS FROM summons WHERE user_id = :userId ORDER BY created_at DESC, id DESC LIMIT :limit")
            .param("userId", userId)
            .param("limit", limit)
            .query(ROW_MAPPER)
            .list()

    private companion object {
        const val COLUMNS =
            "id, user_id, banner_code, species_code, rarity, cost_coins, was_pity, was_duplicate, user_tomo_id, created_at"

        val ROW_MAPPER = RowMapper { rs, _ ->
            Summon(
                id = rs.getObject("id", UUID::class.java),
                userId = rs.getObject("user_id", UUID::class.java),
                bannerCode = rs.getString("banner_code"),
                speciesCode = rs.getString("species_code"),
                rarity = Rarity.valueOf(rs.getString("rarity")),
                costCoins = rs.getInt("cost_coins"),
                wasPity = rs.getBoolean("was_pity"),
                wasDuplicate = rs.getBoolean("was_duplicate"),
                userTomoId = rs.getObject("user_tomo_id", UUID::class.java),
                createdAt = rs.getTimestamp("created_at").toInstant(),
            )
        }
    }
}

package com.zhangs_tech.runtomo.server.food

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class PostgresFoodInventoryRepository(private val jdbcClient: JdbcClient) : FoodInventoryRepository {
    override fun findAll(userId: UUID): Map<String, Int> =
        jdbcClient
            .sql("SELECT food_code, quantity FROM user_food_inventory WHERE user_id = :userId")
            .param("userId", userId)
            .query { rs, _ -> rs.getString("food_code") to rs.getInt("quantity") }
            .list()
            .toMap()

    override fun add(userId: UUID, foodCode: String) {
        jdbcClient
            .sql(
                """
                INSERT INTO user_food_inventory (user_id, food_code, quantity) VALUES (:userId, :foodCode, 1)
                ON CONFLICT (user_id, food_code) DO UPDATE SET quantity = user_food_inventory.quantity + 1
                """.trimIndent()
            )
            .param("userId", userId)
            .param("foodCode", foodCode)
            .update()
    }

    override fun removeOne(userId: UUID, foodCode: String): Boolean =
        jdbcClient
            .sql(
                """
                UPDATE user_food_inventory SET quantity = quantity - 1
                WHERE user_id = :userId AND food_code = :foodCode AND quantity > 0
                """.trimIndent()
            )
            .param("userId", userId)
            .param("foodCode", foodCode)
            .update() == 1
}

@Repository
class PostgresFeedingRepository(private val jdbcClient: JdbcClient) : FeedingRepository {
    override fun count(tomoId: UUID, localDate: LocalDate): Int =
        jdbcClient
            .sql("SELECT count(*) FROM feedings WHERE user_tomo_id = :tomoId AND local_date = :localDate")
            .param("tomoId", tomoId)
            .param("localDate", localDate)
            .query(Int::class.java)
            .single()

    override fun create(tomoId: UUID, foodCode: String, localDate: LocalDate) {
        jdbcClient
            .sql("INSERT INTO feedings (user_tomo_id, food_code, local_date) VALUES (:tomoId, :foodCode, :localDate)")
            .param("tomoId", tomoId)
            .param("foodCode", foodCode)
            .param("localDate", localDate)
            .update()
    }
}

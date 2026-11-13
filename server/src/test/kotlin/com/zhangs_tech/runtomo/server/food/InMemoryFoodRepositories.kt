package com.zhangs_tech.runtomo.server.food

import java.time.LocalDate
import java.util.UUID

class InMemoryFoodInventoryRepository : FoodInventoryRepository {
    val stock = mutableMapOf<Pair<UUID, String>, Int>()

    override fun findAll(userId: UUID): Map<String, Int> =
        stock.filterKeys { it.first == userId }.mapKeys { it.key.second }

    override fun add(userId: UUID, foodCode: String) {
        stock.merge(userId to foodCode, 1, Int::plus)
    }

    override fun removeOne(userId: UUID, foodCode: String): Boolean {
        val quantity = stock[userId to foodCode] ?: 0
        if (quantity <= 0) return false
        stock[userId to foodCode] = quantity - 1
        return true
    }
}

class InMemoryFeedingRepository : FeedingRepository {
    val feedings = mutableListOf<Triple<UUID, String, LocalDate>>()

    override fun count(tomoId: UUID, localDate: LocalDate): Int =
        feedings.count { it.first == tomoId && it.third == localDate }

    override fun create(tomoId: UUID, foodCode: String, localDate: LocalDate) {
        feedings += Triple(tomoId, foodCode, localDate)
    }
}

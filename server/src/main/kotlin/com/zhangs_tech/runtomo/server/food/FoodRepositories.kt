package com.zhangs_tech.runtomo.server.food

import java.time.LocalDate
import java.util.UUID

interface FoodInventoryRepository {
    /** Food code to quantity. Foods the Player never had are missing. */
    fun findAll(userId: UUID): Map<String, Int>
    fun add(userId: UUID, foodCode: String)

    /** Returns false if the Player has none of this food. */
    fun removeOne(userId: UUID, foodCode: String): Boolean
}

interface FeedingRepository {
    fun count(tomoId: UUID, localDate: LocalDate): Int
    fun create(tomoId: UUID, foodCode: String, localDate: LocalDate)
}

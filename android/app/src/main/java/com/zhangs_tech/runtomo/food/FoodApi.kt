package com.zhangs_tech.runtomo.food

import com.zhangs_tech.runtomo.tomo.Tomo
import kotlinx.serialization.Serializable

@Serializable
data class FoodItem(val code: String, val name: String, val cost: Int, val bond: Int, val quantity: Int)

/** The foods with the Player's stock, and the equipped Tomo that eats them (UC-18 step 1). */
@Serializable
data class Pantry(
    val items: List<FoodItem>,
    val coinBalance: Long,
    val tomo: Tomo? = null,
    val feedsLeftToday: Int,
)

sealed interface BuyAttempt {
    data class Bought(val pantry: Pantry) : BuyAttempt

    /** UC-18 2a. */
    data class NotEnoughCoins(val shortfall: Long, val distanceNeededM: Long) : BuyAttempt
}

sealed interface FeedAttempt {
    /** [pantry] holds the fed Tomo with its new Bond. */
    data class Fed(val bondLevelBefore: Int, val pantry: Pantry) : FeedAttempt

    /** UC-18 6a: the Tomo is full. The Player keeps the food. */
    data object Full : FeedAttempt
}

/** The RunTomo server food endpoints (UC-18). Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface FoodApi {
    suspend fun getPantry(): Pantry
    suspend fun buy(foodCode: String): BuyAttempt
    suspend fun feed(tomoId: String, foodCode: String): FeedAttempt
}

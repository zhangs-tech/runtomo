package com.zhangs_tech.runtomo.server.food.dto

import com.zhangs_tech.runtomo.server.food.Pantry
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.tomo.dto.TomoResponse

data class FoodItemResponse(val code: String, val name: String, val cost: Int, val bond: Int, val quantity: Int)

data class PantryResponse(
    val items: List<FoodItemResponse>,
    val coinBalance: Long,
    val tomo: TomoResponse?,
    val feedsLeftToday: Int,
) {
    companion object {
        fun from(pantry: Pantry, gameData: GameData) = PantryResponse(
            items = pantry.stock.map { FoodItemResponse(it.food.code, it.food.name, it.food.cost, it.food.bond, it.quantity) },
            coinBalance = pantry.coinBalance,
            tomo = pantry.tomo?.let { TomoResponse.from(it, gameData.findSpecies(it.speciesCode)!!) },
            feedsLeftToday = pantry.feedsLeftToday,
        )
    }
}

data class FeedRequest(val foodCode: String)

/** [pantry] holds the fed Tomo with its new Bond. */
data class FeedResponse(val bondLevelBefore: Int, val pantry: PantryResponse)

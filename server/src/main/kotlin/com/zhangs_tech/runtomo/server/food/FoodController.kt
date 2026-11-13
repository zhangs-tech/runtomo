package com.zhangs_tech.runtomo.server.food

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.food.dto.FeedRequest
import com.zhangs_tech.runtomo.server.food.dto.FeedResponse
import com.zhangs_tech.runtomo.server.food.dto.PantryResponse
import com.zhangs_tech.runtomo.server.game.GameData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class FoodController(private val foodService: FoodService, private val gameData: GameData) {
    @GetMapping("/food")
    fun getPantry(@CurrentUserId userId: UUID): PantryResponse = PantryResponse.from(foodService.pantry(userId), gameData)

    @PostMapping("/food/{code}/purchase")
    fun buy(@CurrentUserId userId: UUID, @PathVariable code: String): PantryResponse =
        PantryResponse.from(foodService.buy(userId, code), gameData)

    @PostMapping("/me/tomos/{id}/feed")
    fun feed(@CurrentUserId userId: UUID, @PathVariable id: UUID, @RequestBody request: FeedRequest): FeedResponse {
        val result = foodService.feed(userId, id, request.foodCode)
        return FeedResponse(result.bondLevelBefore, PantryResponse.from(result.pantry, gameData))
    }
}

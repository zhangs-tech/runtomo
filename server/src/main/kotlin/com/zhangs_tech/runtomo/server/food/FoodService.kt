package com.zhangs_tech.runtomo.server.food

import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.CoinRepository
import com.zhangs_tech.runtomo.server.coin.NotEnoughCoinsException
import com.zhangs_tech.runtomo.server.game.Food
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.tomo.Tomo
import com.zhangs_tech.runtomo.server.tomo.TomoNotFoundException
import com.zhangs_tech.runtomo.server.tomo.TomoRepository
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class FoodStock(val food: Food, val quantity: Int)

/** [tomo] is the equipped Tomo, which eats the food. */
data class Pantry(val stock: List<FoodStock>, val coinBalance: Long, val tomo: Tomo?, val feedsLeftToday: Int)

data class FeedResult(val tomo: Tomo, val bondLevelBefore: Int, val pantry: Pantry)

/** UC-18 Feed Tomo. */
@Service
class FoodService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val rewardRules: RewardRules,
    private val userRepository: UserRepository,
    private val tomoRepository: TomoRepository,
    private val coinRepository: CoinRepository,
    private val inventoryRepository: FoodInventoryRepository,
    private val feedingRepository: FeedingRepository,
) {
    /** The foods, how many the Player has, and how many more times the equipped Tomo can eat today. */
    fun pantry(userId: UUID): Pantry {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException()
        val inventory = inventoryRepository.findAll(userId)
        val tomo = user.equippedTomoId?.let(tomoRepository::findById)
        val feedsLeft = tomo?.let { feedsLeftToday(it.id) } ?: 0
        return Pantry(gameData.foods.map { FoodStock(it, inventory[it.code] ?: 0) }, user.coinBalance, tomo, feedsLeft)
    }

    @Transactional
    fun buy(userId: UUID, foodCode: String): Pantry {
        val food = gameData.findFood(foodCode) ?: throw FoodNotFoundException()
        // The row lock keeps two purchases at once from spending the same coins.
        val user = userRepository.lockById(userId) ?: throw UserNotFoundException()
        if (user.coinBalance < food.cost) {
            val shortfall = food.cost - user.coinBalance
            throw NotEnoughCoinsException(shortfall, rewardRules.distanceToEarnM(shortfall))
        }
        coinRepository.record(userId, -food.cost, CoinReason.FOOD, refId = null)
        inventoryRepository.add(userId, food.code)
        return pantry(userId)
    }

    /** Uses 1 food, saves the feeding and adds Bond in one transaction. */
    @Transactional
    fun feed(userId: UUID, tomoId: UUID, foodCode: String): FeedResult {
        val food = gameData.findFood(foodCode) ?: throw FoodNotFoundException()
        // The row lock keeps two feedings at once from passing the daily limit.
        userRepository.lockById(userId) ?: throw UserNotFoundException()
        val tomo = tomoRepository.findById(tomoId)?.takeIf { it.userId == userId } ?: throw TomoNotFoundException()
        if (feedsLeftToday(tomo.id) <= 0) throw FeedingLimitException()
        if (!inventoryRepository.removeOne(userId, food.code)) throw NoFoodLeftException()

        feedingRepository.create(tomo.id, food.code, gameCalendar.today())
        val bondPoints = tomo.bondPoints + food.bond
        val fed = tomo.copy(bondPoints = bondPoints, bondLevel = rewardRules.bondLevel(bondPoints))
        tomoRepository.updateProgress(fed.id, fed.distanceTogetherM, fed.bondPoints, fed.bondLevel)
        return FeedResult(fed, tomo.bondLevel, pantry(userId))
    }

    private fun feedsLeftToday(tomoId: UUID): Int =
        (gameData.tunables.feedsPerTomoPerDay - feedingRepository.count(tomoId, gameCalendar.today())).coerceAtLeast(0)
}

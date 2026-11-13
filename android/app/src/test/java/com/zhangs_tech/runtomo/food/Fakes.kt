package com.zhangs_tech.runtomo.food

import com.zhangs_tech.runtomo.tomo.testTomo
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

fun testPantry(bananas: Int = 1, feedsLeft: Int = 3, bondPoints: Int = 0, bondLevel: Int = 1) = Pantry(
    items = listOf(FoodItem("banana", "Banana", 50, 3, bananas), FoodItem("bento", "Bento", 150, 10, 0)),
    coinBalance = 200,
    tomo = testTomo(bondPoints = bondPoints).copy(bondLevel = bondLevel),
    feedsLeftToday = feedsLeft,
)

class FakeFoodApi : FoodApi {
    var pantry = testPantry()
    var buyAttempt: BuyAttempt = BuyAttempt.Bought(testPantry(bananas = 2))
    var feedAttempt: FeedAttempt = FeedAttempt.Fed(bondLevelBefore = 1, pantry = testPantry(bananas = 0, feedsLeft = 2, bondPoints = 3))
    var error: Exception? = null
    val fed = mutableListOf<Pair<String, String>>()

    override suspend fun getPantry(): Pantry {
        error?.let { throw it }
        return pantry
    }

    override suspend fun buy(foodCode: String): BuyAttempt {
        error?.let { throw it }
        return buyAttempt
    }

    override suspend fun feed(tomoId: String, foodCode: String): FeedAttempt {
        error?.let { throw it }
        fed += tomoId to foodCode
        return feedAttempt
    }
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [FoodModule::class])
object FakeFoodModule {
    @Provides
    @Singleton
    fun provideFoodApi(): FoodApi = FakeFoodApi()
}

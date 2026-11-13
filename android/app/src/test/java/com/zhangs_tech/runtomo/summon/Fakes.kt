package com.zhangs_tech.runtomo.summon

import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.testLuppy
import com.zhangs_tech.runtomo.tomo.testNova
import com.zhangs_tech.runtomo.tomo.testTomo
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

fun testBanner(coinBalance: Long = 1500) = Banner(
    code = "standard",
    name = "Standard Egg",
    cost = 1000,
    coinBalance = coinBalance,
    pityRemaining = 20,
    pool = listOf(PoolEntry(testLuppy, 90.0), PoolEntry(testNova, 10.0)),
    rarityRates = listOf(RarityRate(Rarity.COMMON, 90.0), RarityRate(Rarity.EPIC, 10.0)),
)

fun testSummonResult(id: String = "s2", duplicate: Boolean = false) = SummonResult(
    id = id,
    species = testNova,
    wasPity = false,
    wasDuplicate = duplicate,
    duplicateCoins = if (duplicate) 200 else 0,
    tomo = if (duplicate) null else testTomo("t9", testNova, "Nova"),
    coinBalance = 500,
)

class FakeSummonApi : SummonApi {
    var banner = testBanner()
    var bannerError: Exception? = null
    var attempt: SummonAttempt = SummonAttempt.Success(testSummonResult())
    var summonError: Exception? = null
    var latestResult: SummonResult? = SummonResult("s1", testLuppy, false, false, 0, testTomo(), 1500)
    var latestError: Exception? = null
    var summons = 0

    override suspend fun getBanner(code: String): Banner {
        bannerError?.let { throw it }
        return banner
    }

    override suspend fun summon(bannerCode: String): SummonAttempt {
        summons++
        summonError?.let { throw it }
        return attempt
    }

    override suspend fun latest(): SummonResult? {
        latestError?.let { throw it }
        return latestResult
    }
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SummonModule::class])
object FakeSummonModule {
    @Provides
    @Singleton
    fun provideSummonApi(): SummonApi = FakeSummonApi()
}

package com.zhangs_tech.runtomo.server.game

data class CoinTier(val upToM: Int, val coinsPerKm: Int)

data class Tunables(
    val strideM: Double,
    val maxStepsPerDay: Int,
    val coinTiers: List<CoinTier>,
    val bondPerKm: Int,
    val bondLevelThresholds: List<Int>,
    val rewardScreenMinDistanceM: Int,
    val sleepStartHour: Int,
    val sleepEndHour: Int,
    val activeDistanceM: Int,
) {
    /** UC-08 BR-1: the daily goal is the full-reward coin tier. */
    val dailyGoalM: Int get() = coinTiers.first().upToM

    /** Above this distance, a day pays no more TomoCoins (UC-05 2a). */
    val coinCapM: Int get() = coinTiers.last().upToM
}

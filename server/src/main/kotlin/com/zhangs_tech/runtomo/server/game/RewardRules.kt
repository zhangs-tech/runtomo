package com.zhangs_tech.runtomo.server.game

import org.springframework.stereotype.Component
import kotlin.math.floor
import kotlin.math.min

/** The formulas of UC-03 BR-3 and UC-05. All values are for one whole day. */
@Component
class RewardRules(gameData: GameData) {
    private val tunables = gameData.tunables

    fun distanceM(steps: Int): Int = floor(steps * tunables.strideM).toInt()

    fun coinsFor(distanceM: Int): Int {
        var coinMeters = 0L
        var tierStartM = 0
        for (tier in tunables.coinTiers) {
            val metersInTier = (min(distanceM, tier.upToM) - tierStartM).coerceAtLeast(0)
            coinMeters += metersInTier.toLong() * tier.coinsPerKm
            tierStartM = tier.upToM
        }
        return (coinMeters / 1000).toInt()
    }

    fun bondFor(distanceM: Int): Int = (distanceM.toLong() * tunables.bondPerKm / 1000).toInt()

    fun bondLevel(bondPoints: Int): Int = tunables.bondLevelThresholds.count { bondPoints >= it }

    /** About how far the Player must walk to earn [coins], at the first tier's rate. */
    fun distanceToEarnM(coins: Long): Long = coins * 1000 / tunables.coinTiers.first().coinsPerKm
}

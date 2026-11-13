package com.zhangs_tech.runtomo.summon

import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.Tomo
import kotlinx.serialization.Serializable

@Serializable
data class PoolEntry(val species: Species, val ratePercent: Double)

@Serializable
data class RarityRate(val rarity: Rarity, val ratePercent: Double)

/** UC-12 step 1. The rates come from the server, so the app shows the rates the server uses (BR-5). */
@Serializable
data class Banner(
    val code: String,
    val name: String,
    val cost: Int,
    val coinBalance: Long,
    /** Summons until Epic or higher is certain, or null if this banner has no pity. */
    val pityRemaining: Int? = null,
    val pool: List<PoolEntry>,
    val rarityRates: List<RarityRate>,
)

@Serializable
data class SummonResult(
    val id: String,
    val species: Species,
    val wasPity: Boolean,
    val wasDuplicate: Boolean,
    val duplicateCoins: Int,
    /** The new Tomo, or null for a duplicate. */
    val tomo: Tomo? = null,
    val coinBalance: Long,
)

sealed interface SummonAttempt {
    data class Success(val result: SummonResult) : SummonAttempt

    /** UC-12 4a. [distanceNeededM] is about how far the Player must walk to earn the shortfall. */
    data class NotEnoughCoins(val shortfall: Long, val distanceNeededM: Long) : SummonAttempt
}

/** The RunTomo server summon endpoints. Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface SummonApi {
    suspend fun getBanner(code: String): Banner

    suspend fun summon(bannerCode: String): SummonAttempt

    /** The Player's newest summon, or null if they never summoned (UC-12 6a). */
    suspend fun latest(): SummonResult?
}

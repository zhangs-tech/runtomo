package com.zhangs_tech.runtomo.server.summon.dto

import com.zhangs_tech.runtomo.server.game.Rarity
import com.zhangs_tech.runtomo.server.summon.BannerView
import com.zhangs_tech.runtomo.server.summon.SummonResult
import com.zhangs_tech.runtomo.server.tomo.dto.SpeciesResponse
import com.zhangs_tech.runtomo.server.tomo.dto.TomoResponse
import java.time.Instant
import java.util.UUID

/** [ratePercent] comes from the same weights as the roll (BR-5). */
data class PoolEntryResponse(val species: SpeciesResponse, val ratePercent: Double)

data class RarityRateResponse(val rarity: Rarity, val ratePercent: Double)

data class BannerResponse(
    val code: String,
    val name: String,
    val cost: Int,
    val coinBalance: Long,
    val pityRemaining: Int?,
    val pool: List<PoolEntryResponse>,
    val rarityRates: List<RarityRateResponse>,
) {
    companion object {
        fun from(view: BannerView): BannerResponse {
            val total = view.banner.totalWeight.toDouble()
            return BannerResponse(
                code = view.banner.code,
                name = view.banner.name,
                cost = view.banner.cost,
                coinBalance = view.coinBalance,
                pityRemaining = view.pityRemaining,
                pool = view.pool.map { PoolEntryResponse(SpeciesResponse.from(it.species), it.weight * 100 / total) },
                rarityRates = view.pool.groupBy { it.species.rarity }.toSortedMap().map { (rarity, entries) ->
                    RarityRateResponse(rarity, entries.sumOf { it.weight } * 100 / total)
                },
            )
        }
    }
}

data class SummonRequest(val bannerCode: String)

data class SummonResponse(
    val id: UUID,
    val bannerCode: String,
    val species: SpeciesResponse,
    val wasPity: Boolean,
    val wasDuplicate: Boolean,
    val duplicateCoins: Int,
    /** The new Tomo, or null for a duplicate. */
    val tomo: TomoResponse?,
    val coinBalance: Long,
    val createdAt: Instant,
) {
    companion object {
        fun from(result: SummonResult) = SummonResponse(
            id = result.summon.id,
            bannerCode = result.summon.bannerCode,
            species = SpeciesResponse.from(result.species),
            wasPity = result.summon.wasPity,
            wasDuplicate = result.summon.wasDuplicate,
            duplicateCoins = result.duplicateCoins,
            tomo = result.tomo?.let { TomoResponse.from(it, result.species) },
            coinBalance = result.coinBalance,
            createdAt = result.summon.createdAt,
        )
    }
}

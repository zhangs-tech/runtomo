package com.zhangs_tech.runtomo.server.game

data class BannerSpecies(val code: String, val weight: Int)

data class Banner(
    val code: String,
    val name: String,
    val cost: Int,
    val pityThreshold: Int,
    val duplicateCoins: Int,
    val species: List<BannerSpecies>,
) {
    val totalWeight: Int get() = species.sumOf { it.weight }
}

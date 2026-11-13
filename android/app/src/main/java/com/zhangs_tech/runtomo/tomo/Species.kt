package com.zhangs_tech.runtomo.tomo

import kotlinx.serialization.Serializable

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }

@Serializable
data class Species(
    val code: String,
    val name: String,
    val rarity: Rarity,
    val description: String,
)

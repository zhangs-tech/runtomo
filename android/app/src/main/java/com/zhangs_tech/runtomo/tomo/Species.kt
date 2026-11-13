package com.zhangs_tech.runtomo.tomo

enum class Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }

data class Species(
    val code: String,
    val name: String,
    val rarity: Rarity,
    val description: String,
)

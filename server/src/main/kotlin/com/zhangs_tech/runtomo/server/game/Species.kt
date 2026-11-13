package com.zhangs_tech.runtomo.server.game

data class Species(
    val code: String,
    val name: String,
    val rarity: Rarity,
    val starter: Boolean = false,
    val description: String,
)

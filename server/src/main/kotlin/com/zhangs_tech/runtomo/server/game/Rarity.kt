package com.zhangs_tech.runtomo.server.game

enum class Rarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

    /** UC-12 BR-3: pity guarantees one of these. */
    val isEpicOrHigher: Boolean get() = this >= EPIC
}

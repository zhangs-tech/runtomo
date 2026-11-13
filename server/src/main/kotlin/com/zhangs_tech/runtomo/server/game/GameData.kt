package com.zhangs_tech.runtomo.server.game

/** Game data from `resources/game/`. Player rows refer to it by code. */
data class GameData(
    val species: List<Species>,
    val tunables: Tunables,
    val quests: List<Quest>,
) {
    val starters: List<Species> get() = species.filter { it.starter }

    fun findSpecies(code: String): Species? = species.find { it.code == code }

    fun findQuest(code: String): Quest? = quests.find { it.code == code }
}

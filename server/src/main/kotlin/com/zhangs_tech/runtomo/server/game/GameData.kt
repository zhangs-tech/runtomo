package com.zhangs_tech.runtomo.server.game

/** Game data from `resources/game/`. Player rows refer to it by code. */
data class GameData(val species: List<Species>) {
    val starters: List<Species> get() = species.filter { it.starter }

    fun findSpecies(code: String): Species? = species.find { it.code == code }
}

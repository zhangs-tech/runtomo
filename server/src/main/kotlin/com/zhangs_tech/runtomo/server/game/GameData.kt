package com.zhangs_tech.runtomo.server.game

/** Game data from `resources/game/`. Player rows refer to it by code. */
data class GameData(
    val species: List<Species>,
    val tunables: Tunables,
    val quests: List<Quest>,
    val banners: List<Banner>,
    val foods: List<Food>,
) {
    init {
        for (banner in banners) {
            for (entry in banner.species) {
                require(findSpecies(entry.code) != null) { "Banner ${banner.code} has unknown species ${entry.code}" }
                require(entry.weight > 0) { "Banner ${banner.code} has a weight of 0 or less for ${entry.code}" }
            }
        }
    }

    val starters: List<Species> get() = species.filter { it.starter }

    fun findSpecies(code: String): Species? = species.find { it.code == code }

    fun findQuest(code: String): Quest? = quests.find { it.code == code }

    fun findBanner(code: String): Banner? = banners.find { it.code == code }

    fun findFood(code: String): Food? = foods.find { it.code == code }
}

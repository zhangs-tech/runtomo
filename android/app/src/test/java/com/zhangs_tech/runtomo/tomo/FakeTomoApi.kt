package com.zhangs_tech.runtomo.tomo

class FakeTomoApi : TomoApi {
    var starters = listOf(
        Species("luppy", "Luppy", Rarity.COMMON, "Luppy runs hot."),
        Species("rocky", "Rocky", Rarity.COMMON, "Rocky is in no hurry."),
    )
    var getStartersError: Exception? = null
    var chooseStarterError: Exception? = null
    val chosenStarters = mutableListOf<Pair<String, String>>()

    override suspend fun getStarters(): List<Species> {
        getStartersError?.let { throw it }
        return starters
    }

    override suspend fun chooseStarter(speciesCode: String, nickname: String) {
        chosenStarters += speciesCode to nickname
        chooseStarterError?.let { throw it }
    }
}

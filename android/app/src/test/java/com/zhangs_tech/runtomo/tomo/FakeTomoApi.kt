package com.zhangs_tech.runtomo.tomo

val testLuppy = Species("luppy", "Luppy", Rarity.COMMON, "Luppy runs hot.")
val testRocky = Species("rocky", "Rocky", Rarity.COMMON, "Rocky is in no hurry.")
val testNova = Species("nova", "Nova", Rarity.EPIC, "A rare one.")

fun testTomo(id: String = "t1", species: Species = testLuppy, nickname: String = "Mochi", bondPoints: Int = 0, distanceM: Long = 0) =
    Tomo(id, species, nickname, bondPoints, bondLevel = 1, distanceTogetherM = distanceM, birthday = "2026-11-01")

class FakeTomoApi : TomoApi {
    var starters = listOf(testLuppy, testRocky)
    var getStartersError: Exception? = null
    var chooseStarterError: Exception? = null
    var error: Exception? = null
    val chosenStarters = mutableListOf<Pair<String, String>>()
    val equipped = mutableListOf<String>()
    var collection = TomoCollection(
        entries = listOf(
            CollectionEntry(testLuppy, testTomo("t1", testLuppy, "Mochi", bondPoints = 10, distanceM = 1000)),
            CollectionEntry(testRocky, testTomo("t2", testRocky, "Pebble", bondPoints = 50, distanceM = 500)),
            CollectionEntry(testNova),
        ),
        collectedCount = 2,
        totalCount = 3,
        equippedTomoId = "t1",
    )

    override suspend fun getStarters(): List<Species> {
        getStartersError?.let { throw it }
        return starters
    }

    override suspend fun chooseStarter(speciesCode: String, nickname: String) {
        chosenStarters += speciesCode to nickname
        chooseStarterError?.let { throw it }
    }

    override suspend fun getCollection(): TomoCollection {
        error?.let { throw it }
        return collection
    }

    override suspend fun rename(tomoId: String, nickname: String): Tomo {
        error?.let { throw it }
        val tomo = collection.entries.mapNotNull { it.tomo }.find { it.id == tomoId } ?: testTomo(tomoId)
        return tomo.copy(nickname = nickname)
    }

    override suspend fun equip(tomoId: String) {
        error?.let { throw it }
        equipped += tomoId
    }
}

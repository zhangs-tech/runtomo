package com.zhangs_tech.runtomo.tomo

import kotlinx.serialization.Serializable

@Serializable
data class CollectionEntry(val species: Species, val tomo: Tomo? = null)

/** UC-11: every species in the roster, with the Player's Tomo where they own one. */
@Serializable
data class TomoCollection(
    val entries: List<CollectionEntry>,
    val collectedCount: Int,
    val totalCount: Int,
    val equippedTomoId: String?,
)

/** The RunTomo server Tomo endpoints. Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface TomoApi {
    suspend fun getStarters(): List<Species>

    /** Throws [com.zhangs_tech.runtomo.network.ApiException.Conflict] if the Player already owns a Tomo. */
    suspend fun chooseStarter(speciesCode: String, nickname: String)

    suspend fun getCollection(): TomoCollection

    suspend fun rename(tomoId: String, nickname: String): Tomo

    suspend fun equip(tomoId: String)
}

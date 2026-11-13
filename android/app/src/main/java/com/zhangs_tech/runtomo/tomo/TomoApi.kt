package com.zhangs_tech.runtomo.tomo

/** The RunTomo server Tomo endpoints. Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface TomoApi {
    suspend fun getStarters(): List<Species>

    /** Throws [com.zhangs_tech.runtomo.network.ApiException.Conflict] if the Player already owns a Tomo. */
    suspend fun chooseStarter(speciesCode: String, nickname: String)
}

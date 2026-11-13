package com.zhangs_tech.runtomo.server.tomo.dto

import com.zhangs_tech.runtomo.server.game.Rarity
import com.zhangs_tech.runtomo.server.game.Species

data class SpeciesResponse(
    val code: String,
    val name: String,
    val rarity: Rarity,
    val description: String,
) {
    companion object {
        fun from(species: Species) = SpeciesResponse(species.code, species.name, species.rarity, species.description)
    }
}

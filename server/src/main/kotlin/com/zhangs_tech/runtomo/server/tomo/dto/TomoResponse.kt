package com.zhangs_tech.runtomo.server.tomo.dto

import com.zhangs_tech.runtomo.server.game.Species
import com.zhangs_tech.runtomo.server.tomo.Tomo
import java.time.LocalDate
import java.util.UUID

data class TomoResponse(
    val id: UUID,
    val species: SpeciesResponse,
    val nickname: String,
    val bondPoints: Int,
    val bondLevel: Int,
    val distanceTogetherM: Long,
    val birthday: LocalDate,
) {
    companion object {
        fun from(tomo: Tomo, species: Species) = TomoResponse(
            id = tomo.id,
            species = SpeciesResponse.from(species),
            nickname = tomo.nickname,
            bondPoints = tomo.bondPoints,
            bondLevel = tomo.bondLevel,
            distanceTogetherM = tomo.distanceTogetherM,
            birthday = tomo.birthday,
        )
    }
}

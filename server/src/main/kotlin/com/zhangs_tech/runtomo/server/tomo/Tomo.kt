package com.zhangs_tech.runtomo.server.tomo

import java.time.LocalDate
import java.util.UUID

data class Tomo(
    val id: UUID,
    val userId: UUID,
    val speciesCode: String,
    val nickname: String,
    val bondPoints: Int,
    val bondLevel: Int,
    val distanceTogetherM: Long,
    val birthday: LocalDate,
)

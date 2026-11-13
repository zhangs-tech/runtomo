package com.zhangs_tech.runtomo.tomo

import kotlinx.serialization.Serializable

@Serializable
data class Tomo(
    val id: String,
    val species: Species,
    val nickname: String,
    val bondPoints: Int,
    val bondLevel: Int,
    val distanceTogetherM: Long,
    /** ISO date, for example 2026-11-14. */
    val birthday: String,
)

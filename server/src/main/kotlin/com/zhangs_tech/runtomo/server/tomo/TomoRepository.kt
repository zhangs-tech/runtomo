package com.zhangs_tech.runtomo.server.tomo

import java.time.LocalDate
import java.util.UUID

interface TomoRepository {
    fun findByUserId(userId: UUID): List<Tomo>
    fun findById(id: UUID): Tomo?
    fun updateProgress(id: UUID, distanceTogetherM: Long, bondPoints: Int, bondLevel: Int)
    /** Returns null if the Player owns no Tomo with this [id]. */
    fun rename(id: UUID, userId: UUID, nickname: String): Tomo?
    fun create(userId: UUID, speciesCode: String, nickname: String, birthday: LocalDate): Tomo
}

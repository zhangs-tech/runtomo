package com.zhangs_tech.runtomo.server.tomo

import java.time.LocalDate
import java.util.UUID

interface TomoRepository {
    fun findByUserId(userId: UUID): List<Tomo>
    fun findById(id: UUID): Tomo?
    fun updateProgress(id: UUID, distanceTogetherM: Long, bondPoints: Int, bondLevel: Int)
    fun create(userId: UUID, speciesCode: String, nickname: String, birthday: LocalDate): Tomo
}

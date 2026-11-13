package com.zhangs_tech.runtomo.server.tomo

import java.time.LocalDate
import java.util.UUID

interface TomoRepository {
    fun findByUserId(userId: UUID): List<Tomo>
    fun create(userId: UUID, speciesCode: String, nickname: String, birthday: LocalDate): Tomo
}

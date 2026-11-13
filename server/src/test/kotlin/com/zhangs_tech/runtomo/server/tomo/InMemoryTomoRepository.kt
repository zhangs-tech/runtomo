package com.zhangs_tech.runtomo.server.tomo

import java.time.LocalDate
import java.util.UUID

class InMemoryTomoRepository : TomoRepository {
    val tomos = mutableListOf<Tomo>()

    override fun findByUserId(userId: UUID): List<Tomo> = tomos.filter { it.userId == userId }

    override fun create(userId: UUID, speciesCode: String, nickname: String, birthday: LocalDate): Tomo {
        val tomo = Tomo(
            id = UUID.randomUUID(),
            userId = userId,
            speciesCode = speciesCode,
            nickname = nickname,
            bondPoints = 0,
            bondLevel = 1,
            distanceTogetherM = 0,
            birthday = birthday,
        )
        tomos += tomo
        return tomo
    }
}

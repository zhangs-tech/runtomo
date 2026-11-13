package com.zhangs_tech.runtomo.server.tomo

import java.time.LocalDate
import java.util.UUID

class InMemoryTomoRepository : TomoRepository {
    val tomos = mutableListOf<Tomo>()

    override fun findByUserId(userId: UUID): List<Tomo> = tomos.filter { it.userId == userId }

    override fun findById(id: UUID): Tomo? = tomos.find { it.id == id }

    override fun updateProgress(id: UUID, distanceTogetherM: Long, bondPoints: Int, bondLevel: Int) {
        val index = tomos.indexOfFirst { it.id == id }
        tomos[index] = tomos[index].copy(
            distanceTogetherM = distanceTogetherM,
            bondPoints = bondPoints,
            bondLevel = bondLevel,
        )
    }

    override fun rename(id: UUID, userId: UUID, nickname: String): Tomo? {
        val index = tomos.indexOfFirst { it.id == id && it.userId == userId }
        if (index < 0) return null
        tomos[index] = tomos[index].copy(nickname = nickname)
        return tomos[index]
    }

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

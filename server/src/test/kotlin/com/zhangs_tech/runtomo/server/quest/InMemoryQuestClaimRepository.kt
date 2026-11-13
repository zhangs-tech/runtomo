package com.zhangs_tech.runtomo.server.quest

import java.time.LocalDate
import java.util.UUID

class InMemoryQuestClaimRepository : QuestClaimRepository {
    data class Claim(val id: UUID, val userId: UUID, val questCode: String, val localDate: LocalDate)

    val claims = mutableListOf<Claim>()

    override fun findClaimedCodes(userId: UUID, localDate: LocalDate): Set<String> =
        claims.filter { it.userId == userId && it.localDate == localDate }.map { it.questCode }.toSet()

    override fun findClaimedCodes(userId: UUID, dates: Collection<LocalDate>): Map<LocalDate, Set<String>> =
        claims.filter { it.userId == userId && it.localDate in dates }
            .groupBy({ it.localDate }, { it.questCode })
            .mapValues { it.value.toSet() }

    override fun create(userId: UUID, questCode: String, localDate: LocalDate): UUID? {
        if (questCode in findClaimedCodes(userId, localDate)) return null
        return UUID.randomUUID().also { claims += Claim(it, userId, questCode, localDate) }
    }
}

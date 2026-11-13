package com.zhangs_tech.runtomo.server.quest

import java.time.LocalDate
import java.util.UUID

interface QuestClaimRepository {
    fun findClaimedCodes(userId: UUID, localDate: LocalDate): Set<String>

    /** Returns the new claim ID, or null if the quest is already claimed for that day. */
    fun create(userId: UUID, questCode: String, localDate: LocalDate): UUID?
}

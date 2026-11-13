package com.zhangs_tech.runtomo.quest

data class ClaimResult(val coinBalance: Long, val quests: List<Quest>)

/** The RunTomo server quest endpoints. Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface QuestApi {
    suspend fun claim(questCode: String): ClaimResult
}

package com.zhangs_tech.runtomo.history

import kotlinx.serialization.Serializable

@Serializable
data class ActivityTotal(val steps: Long, val distanceM: Long)

@Serializable
data class PeriodTotals(val week: ActivityTotal, val month: ActivityTotal)

/** One synced day. [coins] includes the quest rewards claimed that day. */
@Serializable
data class HistoryDay(val date: String, val steps: Int, val distanceM: Int, val coins: Int, val bond: Int)

@Serializable
data class HistoryPage(val days: List<HistoryDay>, val nextCursor: String? = null)

/** The RunTomo server history endpoints (UC-07). Throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface HistoryApi {
    suspend fun getTotals(): PeriodTotals

    /** Days newest first. Pass the last page's [HistoryPage.nextCursor] to get older days. */
    suspend fun getDays(cursor: String?): HistoryPage
}

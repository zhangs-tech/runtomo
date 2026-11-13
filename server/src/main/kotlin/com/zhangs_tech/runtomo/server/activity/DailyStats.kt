package com.zhangs_tech.runtomo.server.activity

import java.time.LocalDate
import java.util.UUID

/** One Player's totals for one UTC+7 day. [activityCoins] and [bondPoints] are what the day already paid. */
data class DailyStats(
    val userId: UUID,
    val localDate: LocalDate,
    val steps: Int = 0,
    val distanceM: Int = 0,
    val activityCoins: Int = 0,
    val bondPoints: Int = 0,
)

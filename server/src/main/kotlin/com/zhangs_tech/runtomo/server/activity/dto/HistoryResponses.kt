package com.zhangs_tech.runtomo.server.activity.dto

import com.zhangs_tech.runtomo.server.activity.ActivityTotal
import com.zhangs_tech.runtomo.server.activity.HistoryDay
import java.time.LocalDate

data class TotalsResponse(val week: ActivityTotal, val month: ActivityTotal)

data class DaysResponse(val days: List<HistoryDay>, val nextCursor: LocalDate?)

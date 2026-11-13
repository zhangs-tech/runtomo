package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.activity.dto.DaysResponse
import com.zhangs_tech.runtomo.server.activity.dto.TotalsResponse
import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/me")
class HistoryController(private val historyService: HistoryService) {
    @GetMapping("/totals")
    fun getTotals(@CurrentUserId userId: UUID): TotalsResponse =
        historyService.totals(userId).let { TotalsResponse(it.week, it.month) }

    @GetMapping("/days")
    fun getDays(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) cursor: LocalDate?,
        @RequestParam(defaultValue = "30") limit: Int,
    ): DaysResponse = historyService.days(userId, cursor, limit).let { DaysResponse(it.days, it.nextCursor) }
}

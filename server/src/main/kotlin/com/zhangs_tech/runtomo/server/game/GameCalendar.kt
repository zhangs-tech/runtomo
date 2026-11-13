package com.zhangs_tech.runtomo.server.game

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

/** Every Player's day runs from 00:00 to 24:00 UTC+7. */
@Component
class GameCalendar(private val clock: Clock) {
    fun today(): LocalDate = LocalDate.now(clock.withZone(ZONE))

    /** Weeks run Monday to Sunday (UC-07 BR-2, UC-16 BR-2). */
    fun startOfWeek(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun startOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(1)

    companion object {
        val ZONE: ZoneOffset = ZoneOffset.ofHours(7)
    }
}

package com.zhangs_tech.runtomo.core

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Every Player's day runs from 00:00 to 24:00 UTC+7. */
object GameDay {
    val ZONE: ZoneOffset = ZoneOffset.ofHours(7)

    fun today(clock: Clock): LocalDate = LocalDate.now(clock.withZone(ZONE))

    fun startOf(date: LocalDate): Instant = date.atStartOfDay(ZONE).toInstant()
}

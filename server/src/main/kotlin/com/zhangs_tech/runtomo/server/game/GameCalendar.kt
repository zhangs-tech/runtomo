package com.zhangs_tech.runtomo.server.game

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/** Every Player's day runs from 00:00 to 24:00 UTC+7. */
@Component
class GameCalendar(private val clock: Clock) {
    fun today(): LocalDate = LocalDate.now(clock.withZone(ZONE))

    companion object {
        val ZONE: ZoneOffset = ZoneOffset.ofHours(7)
    }
}

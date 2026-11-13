package com.zhangs_tech.runtomo.server.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GameCalendarTests {
    @Test
    fun `today uses the UTC+7 day`() {
        assertEquals(LocalDate.of(2026, 11, 14), GameCalendar(testClock).today())
    }
}

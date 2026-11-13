package com.zhangs_tech.runtomo.core

import com.zhangs_tech.runtomo.testClock
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DistanceTest {
    @Test
    fun distanceM_isStepsTimesStrideRoundedDown() {
        assertEquals(1200, distanceM(1600, 0.75))
        assertEquals(0, distanceM(1, 0.75))
    }

    @Test
    fun formatKm_roundsDownToOneDecimal() {
        assertEquals("2.8", formatKm(2800))
        assertEquals("4.9", formatKm(4999))
        assertEquals("0.0", formatKm(0))
        assertEquals("42.1", formatKm(42_195L))
    }

    @Test
    fun gameDay_usesUtcPlus7() {
        assertEquals(LocalDate.of(2026, 11, 14), GameDay.today(testClock))
        assertEquals(Instant.parse("2026-11-13T17:00:00Z"), GameDay.startOf(LocalDate.of(2026, 11, 14)))
    }
}

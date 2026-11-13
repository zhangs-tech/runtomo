package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.testGameData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MoodRulesTests {
    private val rules = MoodRules(testGameData)

    @Test
    fun `reaching the daily goal is excited`() {
        assertEquals(Mood.EXCITED, rules.pick(5000))
    }

    @Test
    fun `1 km today is happy`() {
        assertEquals(Mood.HAPPY, rules.pick(1000))
        assertEquals(Mood.HAPPY, rules.pick(4999))
    }

    @Test
    fun `under 1 km today is relaxed`() {
        assertEquals(Mood.RELAXED, rules.pick(0))
        assertEquals(Mood.RELAXED, rules.pick(999))
    }
}

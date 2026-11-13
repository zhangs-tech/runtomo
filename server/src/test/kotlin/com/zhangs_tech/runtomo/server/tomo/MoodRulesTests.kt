package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.testGameData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MoodRulesTests {
    private val rules = MoodRules(testGameData)

    @Test
    fun `night is sleepy before any other rule`() {
        assertEquals(Mood.SLEEPY, rules.pick(23, 9000, 0))
        assertEquals(Mood.SLEEPY, rules.pick(5, 9000, 0))
    }

    @Test
    fun `daily goal is excited`() {
        assertEquals(Mood.EXCITED, rules.pick(6, 5000, 0))
    }

    @Test
    fun `1 km today is happy`() {
        assertEquals(Mood.HAPPY, rules.pick(12, 1000, 0))
    }

    @Test
    fun `1 km yesterday is relaxed`() {
        assertEquals(Mood.RELAXED, rules.pick(12, 999, 1000))
    }

    @Test
    fun `otherwise bored`() {
        assertEquals(Mood.BORED, rules.pick(22, 0, 0))
    }
}

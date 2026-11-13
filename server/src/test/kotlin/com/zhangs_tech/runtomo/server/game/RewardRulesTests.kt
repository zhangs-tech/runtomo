package com.zhangs_tech.runtomo.server.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RewardRulesTests {
    private val rules = RewardRules(testGameData)

    @Test
    fun `distance is steps times 0_75 m, rounded down`() {
        assertEquals(750, rules.distanceM(1000))
        assertEquals(0, rules.distanceM(1))
    }

    @Test
    fun `coins follow the daily tiers`() {
        assertEquals(0, rules.coinsFor(0))
        assertEquals(120, rules.coinsFor(1200))
        assertEquals(500, rules.coinsFor(5000))
        assertEquals(625, rules.coinsFor(7500))
        assertEquals(750, rules.coinsFor(10_000))
        assertEquals(750, rules.coinsFor(15_000))
    }

    @Test
    fun `bond is 10 per km with no cap`() {
        assertEquals(12, rules.bondFor(1200))
        assertEquals(150, rules.bondFor(15_000))
    }

    @Test
    fun `bond level uses the thresholds`() {
        assertEquals(1, rules.bondLevel(0))
        assertEquals(1, rules.bondLevel(49))
        assertEquals(2, rules.bondLevel(50))
        assertEquals(10, rules.bondLevel(9999))
    }
}

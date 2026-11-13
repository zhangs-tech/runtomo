package com.zhangs_tech.runtomo.server.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class DisplayNameTests {
    @Test
    fun `a name of 3 to 20 characters is valid`() {
        assertTrue(DisplayName.isValid("Kai"))
        assertTrue(DisplayName.isValid("a".repeat(20)))
    }

    @Test
    fun `a name that is too short, too long or padded is not valid`() {
        assertFalse(DisplayName.isValid("Al"))
        assertFalse(DisplayName.isValid("a".repeat(21)))
        assertFalse(DisplayName.isValid(" Kai "))
    }

    @Test
    fun `the default is the Google name when it is valid`() {
        assertEquals("Kai Tan", DisplayName.defaultFor(" Kai Tan "))
    }

    @Test
    fun `the default is Runner plus 4 digits when the Google name is not valid`() {
        assertEquals("Runner0042", DisplayName.defaultFor("Al", FixedRandom(42)))
        assertEquals("Runner0042", DisplayName.defaultFor(null, FixedRandom(42)))
    }

    private class FixedRandom(private val value: Int) : Random() {
        override fun nextBits(bitCount: Int): Int = value
        override fun nextInt(until: Int): Int = value
    }
}

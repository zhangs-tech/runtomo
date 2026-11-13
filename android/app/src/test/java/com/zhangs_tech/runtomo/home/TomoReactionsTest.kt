package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.tomo.Mood
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class TomoReactionsTest {
    private var now = 0L

    /** nextInt always returns 0: every tap shows a speech bubble, and the first line is picked. */
    private val alwaysZero = object : Random() {
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextInt(until: Int): Int = 0
    }

    private val neverSpeak = object : Random() {
        override fun nextBits(bitCount: Int): Int = 0
        override fun nextInt(until: Int): Int = until - 1
    }

    @Test
    fun normalTap_canShowSpeechBubble() {
        val reactions = TomoReactions(alwaysZero) { now }

        assertEquals(TomoReaction.Normal(SpeechLine.READY), reactions.onTap(Mood.HAPPY, 1000, 3))
    }

    @Test
    fun normalTap_withoutLuck_showsNoBubble() {
        val reactions = TomoReactions(neverSpeak) { now }

        assertEquals(TomoReaction.Normal(null), reactions.onTap(Mood.HAPPY, 1000, 3))
    }

    @Test
    fun fiveQuickTaps_isTooMuch() {
        val reactions = TomoReactions(neverSpeak) { now }

        repeat(4) {
            reactions.onTap(Mood.HAPPY, 0, 3)
            now += 300
        }

        assertEquals(TomoReaction.TooMuch, reactions.onTap(Mood.HAPPY, 0, 3))
    }

    @Test
    fun slowTaps_areNeverTooMuch() {
        val reactions = TomoReactions(neverSpeak) { now }

        repeat(10) {
            assertEquals(TomoReaction.Normal(null), reactions.onTap(Mood.HAPPY, 0, 3))
            now += 1000
        }
    }
}

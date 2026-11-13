package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.tomo.Mood
import kotlin.random.Random

enum class SpeechLine { WALK, STEPS_TODAY, FUN, READY }

sealed interface TomoReaction {
    /** UC-09 2b: the Player tapped many times quickly. */
    data object TooMuch : TomoReaction

    data class Normal(val speech: SpeechLine?) : TomoReaction
}

/** UC-09 Interact With Tomo. Tapping changes no data, so this runs only in the app (BR-1). */
class TomoReactions(
    private val random: Random = Random.Default,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val recentTaps = ArrayDeque<Long>()

    fun onTap(mood: Mood, todaySteps: Int, speechBubbleOneIn: Int): TomoReaction {
        val now = nowMillis()
        recentTaps.addLast(now)
        while (recentTaps.first() < now - QUICK_TAP_WINDOW_MILLIS) recentTaps.removeFirst()
        if (recentTaps.size >= QUICK_TAPS) {
            recentTaps.clear()
            return TomoReaction.TooMuch
        }

        val speech = if (random.nextInt(speechBubbleOneIn.coerceAtLeast(1)) == 0) pickLine(mood, todaySteps) else null
        return TomoReaction.Normal(speech)
    }

    /** BR-2: a line can mention the Player's real data. */
    private fun pickLine(mood: Mood, todaySteps: Int): SpeechLine {
        val lines = buildList {
            add(SpeechLine.READY)
            add(SpeechLine.WALK)
            if (todaySteps > 0) add(SpeechLine.STEPS_TODAY)
            if (mood == Mood.HAPPY || mood == Mood.EXCITED) add(SpeechLine.FUN)
        }
        return lines[random.nextInt(lines.size)]
    }

    private companion object {
        const val QUICK_TAPS = 5
        const val QUICK_TAP_WINDOW_MILLIS = 2_000L
    }
}

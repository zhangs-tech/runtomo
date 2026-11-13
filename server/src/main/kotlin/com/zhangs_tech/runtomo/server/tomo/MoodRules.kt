package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.GameData
import org.springframework.stereotype.Component

/** UC-10 BR-1. The mood is never stored (BR-2). */
@Component
class MoodRules(gameData: GameData) {
    private val tunables = gameData.tunables

    fun pick(hourUtc7: Int, todayDistanceM: Int, yesterdayDistanceM: Int): Mood = when {
        hourUtc7 >= tunables.sleepStartHour || hourUtc7 < tunables.sleepEndHour -> Mood.SLEEPY
        todayDistanceM >= tunables.dailyGoalM -> Mood.EXCITED
        todayDistanceM >= tunables.activeDistanceM -> Mood.HAPPY
        yesterdayDistanceM >= tunables.activeDistanceM -> Mood.RELAXED
        else -> Mood.BORED
    }
}

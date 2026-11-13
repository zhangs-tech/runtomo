package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.GameData
import org.springframework.stereotype.Component

/** UC-10 BR-1: the mood depends only on today's distance. It is never stored (BR-2). */
@Component
class MoodRules(gameData: GameData) {
    private val tunables = gameData.tunables

    fun pick(todayDistanceM: Int): Mood = when {
        todayDistanceM >= tunables.dailyGoalM -> Mood.EXCITED
        todayDistanceM >= tunables.activeDistanceM -> Mood.HAPPY
        else -> Mood.RELAXED
    }
}

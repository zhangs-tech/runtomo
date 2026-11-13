package com.zhangs_tech.runtomo.server.home.dto

import com.zhangs_tech.runtomo.server.quest.dto.QuestResponse
import com.zhangs_tech.runtomo.server.tomo.Mood
import com.zhangs_tech.runtomo.server.tomo.dto.TomoResponse
import java.time.LocalDate

data class HomeResponse(
    val displayName: String,
    /** Null only if the Player has not chosen a starter yet. */
    val tomo: TomoResponse?,
    val mood: Mood,
    val today: TodayResponse,
    val dailyGoalM: Int,
    val strideM: Double,
    val coinBalance: Long,
    val quests: List<QuestResponse>,
    val lastSyncDate: LocalDate?,
    /** UC-09 BR-3: a tap on the Tomo shows a speech bubble 1 time in this many. */
    val speechBubbleOneIn: Int,
)

data class TodayResponse(val date: LocalDate, val steps: Int, val distanceM: Int)

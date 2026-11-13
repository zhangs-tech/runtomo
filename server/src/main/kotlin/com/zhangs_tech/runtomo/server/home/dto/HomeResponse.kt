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
)

data class TodayResponse(val date: LocalDate, val steps: Int, val distanceM: Int)

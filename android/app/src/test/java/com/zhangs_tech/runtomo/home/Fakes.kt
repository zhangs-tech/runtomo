package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.quest.ClaimResult
import com.zhangs_tech.runtomo.quest.Quest
import com.zhangs_tech.runtomo.quest.QuestApi
import com.zhangs_tech.runtomo.quest.QuestMetric
import com.zhangs_tech.runtomo.quest.QuestStatus
import com.zhangs_tech.runtomo.tomo.Mood
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.Tomo

fun testHome(today: String = "2026-11-14", lastSyncDate: String? = today) = HomeData(
    displayName = "Kai",
    tomo = Tomo("t1", Species("luppy", "Luppy", Rarity.COMMON, "Runs hot."), "Mochi", 0, 1, 0, "2026-11-01"),
    mood = Mood.RELAXED,
    today = Today(today, 0, 0),
    dailyGoalM = 5000,
    strideM = 0.75,
    coinBalance = 0,
    quests = listOf(
        Quest("walk-1km", "Walk or run 1 km", QuestMetric.DISTANCE, 1000, 0, 100, QuestStatus.IN_PROGRESS),
        Quest("steps-5000", "Take 5,000 steps", QuestMetric.STEPS, 5000, 0, 150, QuestStatus.IN_PROGRESS),
    ),
    lastSyncDate = lastSyncDate,
)

class FakeHomeApi(var home: HomeData = testHome()) : HomeApi {
    var error: Exception? = null
    var calls = 0

    override suspend fun getHome(): HomeData {
        calls++
        error?.let { throw it }
        return home
    }
}

class FakeQuestApi : QuestApi {
    var error: Exception? = null
    val claimed = mutableListOf<String>()

    override suspend fun claim(questCode: String): ClaimResult {
        error?.let { throw it }
        claimed += questCode
        val quests = testHome().quests.map { if (it.code == questCode) it.copy(status = QuestStatus.CLAIMED) else it }
        return ClaimResult(coinBalance = 220, quests = quests)
    }
}

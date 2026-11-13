package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.activity.DaySync
import com.zhangs_tech.runtomo.core.distanceM
import com.zhangs_tech.runtomo.quest.Quest
import com.zhangs_tech.runtomo.quest.QuestStatus
import com.zhangs_tech.runtomo.tomo.Mood
import com.zhangs_tech.runtomo.tomo.Tomo
import kotlinx.serialization.Serializable

/** Everything the Home Screen shows (UC-08). The app caches it for offline use (UC-08 1a). */
@Serializable
data class HomeData(
    val displayName: String,
    val tomo: Tomo?,
    val mood: Mood,
    val today: Today,
    val dailyGoalM: Int,
    val strideM: Double,
    val coinBalance: Long,
    val quests: List<Quest>,
    /** ISO date of the newest synced day, or null if the Player never synced. */
    val lastSyncDate: String?,
    /** UC-09 BR-3: a tap on the Tomo shows a speech bubble 1 time in this many. */
    val speechBubbleOneIn: Int = 3,
) {
    /** Applies the totals of a newer sync. */
    fun withSync(sync: DaySync): HomeData = copy(
        mood = sync.mood,
        today = Today(sync.date, sync.steps, sync.distanceM),
        strideM = sync.strideM,
        coinBalance = sync.coinBalance,
        quests = quests.map { it.withTotals(sync.steps, sync.distanceM) },
        lastSyncDate = sync.date,
        tomo = tomo?.let { tomo ->
            sync.rewards?.let { tomo.copy(bondLevel = it.bondLevelAfter, distanceTogetherM = it.distanceTogetherM) } ?: tomo
        },
    )

    /**
     * The cached data with today's steps from the phone, for offline use (UC-08 1a). A cache from
     * an earlier day starts today at 0.
     */
    fun offline(today: String, phoneSteps: Int?, strideM: Double): HomeData {
        val fromToday = if (this.today.date == today) this else copy(
            today = Today(today, 0, 0),
            quests = quests.map { it.copy(progress = 0, status = QuestStatus.IN_PROGRESS) },
        )
        if (phoneSteps == null || phoneSteps <= fromToday.today.steps) return fromToday
        val distanceM = distanceM(phoneSteps, strideM)
        return fromToday.copy(
            today = Today(today, phoneSteps, distanceM),
            quests = fromToday.quests.map { it.withTotals(phoneSteps, distanceM) },
        )
    }
}

@Serializable
data class Today(val date: String, val steps: Int, val distanceM: Int)

package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.activity.DaySync
import com.zhangs_tech.runtomo.activity.testRewards
import com.zhangs_tech.runtomo.quest.QuestStatus
import com.zhangs_tech.runtomo.tomo.Mood
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDataTest {
    @Test
    fun withSync_updatesTotalsQuestsAndTomo() {
        val sync = DaySync("2026-11-14", 1600, 1200, 0.75, 120, Mood.HAPPY, testRewards(distanceM = 1200))

        val home = testHome().withSync(sync)

        assertEquals(Today("2026-11-14", 1600, 1200), home.today)
        assertEquals(Mood.HAPPY, home.mood)
        assertEquals(120, home.coinBalance)
        assertEquals(QuestStatus.CLAIMABLE, home.quests[0].status)
        assertEquals(QuestStatus.IN_PROGRESS, home.quests[1].status)
        assertEquals(1600, home.quests[1].progress)
        assertEquals(1200, home.tomo!!.distanceTogetherM)
    }

    @Test
    fun withSync_keepsClaimedQuestsClaimed() {
        val claimed = testHome().let { it.copy(quests = it.quests.map { q -> q.copy(status = QuestStatus.CLAIMED) }) }

        val home = claimed.withSync(DaySync("2026-11-14", 9000, 6750, 0.75, 0, Mood.EXCITED))

        assertEquals(QuestStatus.CLAIMED, home.quests[0].status)
    }

    @Test
    fun offline_usesPhoneStepsWithStoredStride() {
        val home = testHome().offline("2026-11-14", phoneSteps = 2000, strideM = 0.75)

        assertEquals(Today("2026-11-14", 2000, 1500), home.today)
        assertEquals(QuestStatus.CLAIMABLE, home.quests[0].status)
    }

    @Test
    fun offline_cacheFromEarlierDay_startsTodayAtZero() {
        val yesterday = testHome(today = "2026-11-13").let {
            it.copy(today = Today("2026-11-13", 9000, 6750), quests = it.quests.map { q -> q.copy(progress = 9000) })
        }

        val home = yesterday.offline("2026-11-14", phoneSteps = null, strideM = 0.75)

        assertEquals(Today("2026-11-14", 0, 0), home.today)
        assertEquals(0, home.quests[0].progress)
    }
}

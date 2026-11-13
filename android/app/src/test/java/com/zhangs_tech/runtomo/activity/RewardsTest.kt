package com.zhangs_tech.runtomo.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardsTest {
    @Test
    fun plus_addsAmountsAndKeepsFirstLevelAndLatestTotals() {
        val first = testRewards(distanceM = 300, showRewardScreen = false).copy(
            bondLevelBefore = 1,
            bondLevelAfter = 1,
            completedQuests = listOf(CompletedQuest("walk-1km", "Walk or run 1 km", 100)),
        )
        val later = testRewards(distanceM = 400, showRewardScreen = false).copy(
            bondLevelBefore = 1,
            bondLevelAfter = 2,
            distanceTogetherM = 5000,
            completedQuests = listOf(CompletedQuest("walk-1km", "Walk or run 1 km", 100)),
        )

        val total = first.plus(later)

        assertEquals(700, total.distanceM)
        assertEquals(70, total.coins)
        assertEquals(7, total.bond)
        assertEquals(1, total.bondLevelBefore)
        assertEquals(2, total.bondLevelAfter)
        assertEquals(5000, total.distanceTogetherM)
        assertEquals(1, total.completedQuests.size)
    }

    @Test
    fun plus_showsScreenIfAnySyncShowsIt() {
        assertTrue(testRewards(showRewardScreen = false).plus(testRewards(showRewardScreen = true)).showRewardScreen)
    }
}

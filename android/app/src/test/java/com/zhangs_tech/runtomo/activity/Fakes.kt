package com.zhangs_tech.runtomo.activity

import com.zhangs_tech.runtomo.tomo.Mood
import java.time.Instant
import java.time.LocalDate

class FakeStepsReader(
    var canRead: Boolean = true,
    var canReadInBackground: Boolean = true,
    var steps: Int = 1600,
) : StepsReader {
    var readError: Exception? = null
    var lastRange: Pair<Instant, Instant>? = null

    override val permissions = setOf("read-steps", "read-in-background")
    override suspend fun canReadSteps(): Boolean = canRead
    override suspend fun canReadInBackground(): Boolean = canRead && canReadInBackground
    override suspend fun readSteps(start: Instant, end: Instant): Int {
        lastRange = start to end
        readError?.let { throw it }
        return steps
    }
}

class FakeActivityApi : ActivityApi {
    var error: Exception? = null
    var rewards: Rewards? = testRewards()
    val syncs = mutableListOf<Pair<LocalDate, Int>>()

    override suspend fun sync(date: LocalDate, steps: Int): DaySync {
        syncs += date to steps
        error?.let { throw it }
        return DaySync(date.toString(), steps, (steps * 0.75).toInt(), 0.75, coinBalance = 120, Mood.HAPPY, rewards)
    }
}

class FakeBackgroundSync : BackgroundSync {
    var isScheduled = false
    override fun schedule() {
        isScheduled = true
    }
    override fun cancel() {
        isScheduled = false
    }
}

fun testRewards(
    distanceM: Int = 1200,
    showRewardScreen: Boolean = true,
) = Rewards(
    distanceM = distanceM,
    coins = distanceM / 10,
    coinCapReached = false,
    bond = distanceM / 100,
    bondLevelBefore = 1,
    bondLevelAfter = 1,
    distanceTogetherM = distanceM.toLong(),
    tomoNickname = "Mochi",
    completedQuests = emptyList(),
    showRewardScreen = showRewardScreen,
)

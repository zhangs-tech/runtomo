package com.zhangs_tech.runtomo.activity

import com.zhangs_tech.runtomo.tomo.Mood
import kotlinx.serialization.Serializable

/** The server's answer to a sync (UC-03): today's stored totals and what the sync paid. */
@Serializable
data class DaySync(
    val date: String,
    val steps: Int,
    val distanceM: Int,
    val strideM: Double,
    val coinBalance: Long,
    val mood: Mood,
    val rewards: Rewards? = null,
)

@Serializable
data class CompletedQuest(val code: String, val title: String, val reward: Int)

/** What one or more syncs paid (UC-05), for the reward screen (UC-06). */
@Serializable
data class Rewards(
    val distanceM: Int,
    val coins: Int,
    val coinCapReached: Boolean,
    val bond: Int,
    val bondLevelBefore: Int,
    val bondLevelAfter: Int,
    val distanceTogetherM: Long,
    val tomoNickname: String?,
    val completedQuests: List<CompletedQuest>,
    val showRewardScreen: Boolean,
) {
    /** Joins the rewards of a later sync, for example several background syncs the Player has not seen. */
    fun plus(later: Rewards) = Rewards(
        distanceM = distanceM + later.distanceM,
        coins = coins + later.coins,
        coinCapReached = later.coinCapReached,
        bond = bond + later.bond,
        bondLevelBefore = bondLevelBefore,
        bondLevelAfter = later.bondLevelAfter,
        distanceTogetherM = later.distanceTogetherM,
        tomoNickname = later.tomoNickname ?: tomoNickname,
        completedQuests = (completedQuests + later.completedQuests).distinctBy { it.code },
        showRewardScreen = showRewardScreen || later.showRewardScreen,
    )
}

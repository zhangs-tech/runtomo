package com.zhangs_tech.runtomo.server.activity.dto

import com.zhangs_tech.runtomo.server.activity.Rewards
import com.zhangs_tech.runtomo.server.activity.SyncResult
import com.zhangs_tech.runtomo.server.tomo.Mood
import java.time.LocalDate

data class SyncResponse(
    val date: LocalDate,
    val steps: Int,
    val distanceM: Int,
    val strideM: Double,
    val coinBalance: Long,
    val mood: Mood,
    val rewards: RewardsResponse?,
) {
    companion object {
        fun from(result: SyncResult, strideM: Double) = SyncResponse(
            date = result.stats.localDate,
            steps = result.stats.steps,
            distanceM = result.stats.distanceM,
            strideM = strideM,
            coinBalance = result.coinBalance,
            mood = result.mood,
            rewards = result.rewards?.let(RewardsResponse::from),
        )
    }
}

data class CompletedQuestResponse(val code: String, val title: String, val reward: Int)

data class RewardsResponse(
    val distanceM: Int,
    val coins: Int,
    val coinCapReached: Boolean,
    val bond: Int,
    val bondLevelBefore: Int,
    val bondLevelAfter: Int,
    val distanceTogetherM: Long,
    val tomoNickname: String?,
    val completedQuests: List<CompletedQuestResponse>,
    val showRewardScreen: Boolean,
) {
    companion object {
        fun from(rewards: Rewards) = RewardsResponse(
            distanceM = rewards.distanceM,
            coins = rewards.coins,
            coinCapReached = rewards.coinCapReached,
            bond = rewards.bond,
            bondLevelBefore = rewards.bondLevelBefore,
            bondLevelAfter = rewards.bondLevelAfter,
            distanceTogetherM = rewards.distanceTogetherM,
            tomoNickname = rewards.tomoNickname,
            completedQuests = rewards.completedQuests.map { CompletedQuestResponse(it.code, it.title, it.reward) },
            showRewardScreen = rewards.showRewardScreen,
        )
    }
}

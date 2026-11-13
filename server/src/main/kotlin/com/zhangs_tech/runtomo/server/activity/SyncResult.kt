package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.game.Quest
import com.zhangs_tech.runtomo.server.tomo.Mood

data class SyncResult(
    val stats: DailyStats,
    val coinBalance: Long,
    val mood: Mood,
    /** Null when the sync brought no new steps. */
    val rewards: Rewards?,
)

/** What one sync paid (UC-05), for the reward screen (UC-06). */
data class Rewards(
    val distanceM: Int,
    val coins: Int,
    val coinCapReached: Boolean,
    val bond: Int,
    val bondLevelBefore: Int,
    val bondLevelAfter: Int,
    val distanceTogetherM: Long,
    val tomoNickname: String?,
    val completedQuests: List<Quest>,
    val showRewardScreen: Boolean,
)

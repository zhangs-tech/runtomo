package com.zhangs_tech.runtomo.cache

import com.zhangs_tech.runtomo.activity.Rewards
import com.zhangs_tech.runtomo.home.HomeData

/** The last step total the server accepted, and the stride it used (UC-03). */
data class AcceptedSync(val date: String, val steps: Int, val strideM: Double)

/** Data of the signed-in Player that the app keeps on the device. Cleared on sign-out. */
interface PlayerCache {
    suspend fun lastAcceptedSync(): AcceptedSync?
    suspend fun saveAcceptedSync(sync: AcceptedSync)

    /** Rewards from syncs that the Player has not seen yet, for example from background syncs. */
    suspend fun pendingRewards(): Rewards?
    suspend fun addPendingRewards(rewards: Rewards)
    suspend fun clearPendingRewards()

    suspend fun home(): HomeData?
    suspend fun saveHome(home: HomeData)

    suspend fun clear()
}

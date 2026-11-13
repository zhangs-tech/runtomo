package com.zhangs_tech.runtomo.activity

import com.zhangs_tech.runtomo.cache.AcceptedSync
import com.zhangs_tech.runtomo.cache.PlayerCache
import com.zhangs_tech.runtomo.core.GameDay
import com.zhangs_tech.runtomo.network.ApiException
import java.time.Clock
import javax.inject.Inject

/** UC-03. The foreground sync and the background worker both use this class. */
class ActivityRepository @Inject constructor(
    private val stepsReader: StepsReader,
    private val activityApi: ActivityApi,
    private val playerCache: PlayerCache,
    private val clock: Clock,
) {
    /** The permissions to request together (UC-03 1a). */
    val stepsPermissions: Set<String> get() = stepsReader.permissions

    suspend fun canReadSteps(): Boolean = stepsReader.canReadSteps()

    suspend fun canReadInBackground(): Boolean = stepsReader.canReadInBackground()

    /** Today's total from Health Connect, from 00:00 UTC+7 to now. Needs the read permission. */
    suspend fun readTodaySteps(): Int =
        stepsReader.readSteps(GameDay.startOf(GameDay.today(clock)), clock.instant())

    /**
     * Sends today's total to the server. Returns null if the server cannot be reached or refuses
     * the sync. The next sync reads Health Connect again, so the app keeps no queue (UC-03 4a, 4c).
     */
    suspend fun sync(steps: Int, skipIfUnchanged: Boolean = false): DaySync? {
        val today = GameDay.today(clock)
        if (skipIfUnchanged) {
            val last = playerCache.lastAcceptedSync()
            if (last != null && last.date == today.toString() && last.steps == steps) return null
        }
        val result = try {
            activityApi.sync(today, steps)
        } catch (e: ApiException) {
            return null
        }
        playerCache.saveAcceptedSync(AcceptedSync(result.date, result.steps, result.strideM))
        result.rewards?.let { playerCache.addPendingRewards(it) }
        return result
    }
}

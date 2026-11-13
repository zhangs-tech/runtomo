package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.CoinRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.tomo.Mood
import com.zhangs_tech.runtomo.server.tomo.MoodRules
import com.zhangs_tech.runtomo.server.tomo.TomoRepository
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/** UC-03 Sync Daily Activity, with UC-05 Calculate Daily Rewards. */
@Service
class ActivityService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val rewardRules: RewardRules,
    private val moodRules: MoodRules,
    private val dailyStatsRepository: DailyStatsRepository,
    private val userRepository: UserRepository,
    private val tomoRepository: TomoRepository,
    private val coinRepository: CoinRepository,
) {
    @Transactional
    fun sync(userId: UUID, date: LocalDate, steps: Int): SyncResult {
        if (date != gameCalendar.today()) throw DayClosedException()
        val maxSteps = gameData.tunables.maxStepsPerDay
        if (steps !in 0..maxSteps) throw InvalidStepsException(maxSteps)

        // The row lock stops two syncs at once from paying the same steps twice (UC-05).
        val before = dailyStatsRepository.lockOrCreate(userId, date)

        // UC-03 4b: the stored total only goes up, and a sync never takes rewards back.
        if (steps <= before.steps) {
            dailyStatsRepository.save(before)
            val user = userRepository.findById(userId) ?: throw UserNotFoundException()
            return SyncResult(before, user.coinBalance, currentMood(userId, before.distanceM), rewards = null)
        }

        val distanceM = rewardRules.distanceM(steps)
        val newDistanceM = distanceM - before.distanceM
        val dayCoins = rewardRules.coinsFor(distanceM)
        val newCoins = dayCoins - before.activityCoins

        val user = userRepository.findById(userId) ?: throw UserNotFoundException()
        // BR-6: the Tomo equipped at sync time gets the Bond and Distance Together.
        val tomo = user.equippedTomoId?.let(tomoRepository::findById)
        val dayBond = if (tomo != null) rewardRules.bondFor(distanceM) else before.bondPoints
        val newBond = dayBond - before.bondPoints
        val tomoAfter = tomo?.copy(
            distanceTogetherM = tomo.distanceTogetherM + newDistanceM,
            bondPoints = tomo.bondPoints + newBond,
            bondLevel = rewardRules.bondLevel(tomo.bondPoints + newBond),
        )
        tomoAfter?.let { tomoRepository.updateProgress(it.id, it.distanceTogetherM, it.bondPoints, it.bondLevel) }

        val coinBalance = if (newCoins > 0) {
            coinRepository.record(userId, newCoins, CoinReason.ACTIVITY, refId = null)
        } else {
            user.coinBalance
        }

        val after = before.copy(steps = steps, distanceM = distanceM, activityCoins = dayCoins, bondPoints = dayBond)
        dailyStatsRepository.save(after)

        val completedQuests = gameData.quests.filter {
            !it.isComplete(before.steps, before.distanceM) && it.isComplete(after.steps, after.distanceM)
        }
        val levelUp = tomo != null && tomoAfter!!.bondLevel > tomo.bondLevel
        val rewards = Rewards(
            distanceM = newDistanceM,
            coins = newCoins,
            coinCapReached = distanceM >= gameData.tunables.coinCapM,
            bond = newBond,
            bondLevelBefore = tomo?.bondLevel ?: 0,
            bondLevelAfter = tomoAfter?.bondLevel ?: 0,
            distanceTogetherM = tomoAfter?.distanceTogetherM ?: 0,
            tomoNickname = tomo?.nickname,
            completedQuests = completedQuests,
            showRewardScreen = newDistanceM >= gameData.tunables.rewardScreenMinDistanceM ||
                levelUp || completedQuests.isNotEmpty(),
        )
        return SyncResult(after, coinBalance, currentMood(userId, distanceM), rewards)
    }

    /** UC-10. A day with no row counts as 0 km. */
    fun currentMood(userId: UUID, todayDistanceM: Int): Mood {
        val yesterday = gameCalendar.today().minusDays(1)
        val yesterdayDistanceM = dailyStatsRepository.find(userId, yesterday)?.distanceM ?: 0
        return moodRules.pick(gameCalendar.now().hour, todayDistanceM, yesterdayDistanceM)
    }
}

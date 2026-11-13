package com.zhangs_tech.runtomo.server.quest

import com.zhangs_tech.runtomo.server.activity.DailyStats
import com.zhangs_tech.runtomo.server.activity.DailyStatsRepository
import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.CoinRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * UC-13. Progress is not stored: it is today's steps or distance from `daily_stats`.
 * The Player claims each reward. A quest not claimed before the day ends pays nothing (4a).
 */
@Service
class QuestService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val dailyStatsRepository: DailyStatsRepository,
    private val questClaimRepository: QuestClaimRepository,
    private val coinRepository: CoinRepository,
) {
    fun todayQuests(userId: UUID): List<QuestProgress> {
        val today = gameCalendar.today()
        val stats = dailyStatsRepository.find(userId, today) ?: DailyStats(userId, today)
        val claimed = questClaimRepository.findClaimedCodes(userId, today)
        return gameData.quests.map { quest ->
            val status = when {
                quest.code in claimed -> QuestStatus.CLAIMED
                quest.isComplete(stats.steps, stats.distanceM) -> QuestStatus.CLAIMABLE
                else -> QuestStatus.IN_PROGRESS
            }
            QuestProgress(quest, quest.progress(stats.steps, stats.distanceM), status)
        }
    }

    /** Returns the new TomoCoin balance. */
    @Transactional
    fun claim(userId: UUID, questCode: String): Long {
        val quest = gameData.findQuest(questCode) ?: throw QuestNotFoundException()
        val today = gameCalendar.today()
        val stats = dailyStatsRepository.find(userId, today) ?: DailyStats(userId, today)
        if (!quest.isComplete(stats.steps, stats.distanceM)) throw QuestNotCompleteException()
        val claimId = questClaimRepository.create(userId, quest.code, today) ?: throw QuestAlreadyClaimedException()
        return coinRepository.record(userId, quest.reward, CoinReason.QUEST, claimId)
    }
}

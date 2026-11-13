package com.zhangs_tech.runtomo.server.social

import com.zhangs_tech.runtomo.server.activity.DailyStatsRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

enum class LeaderboardPeriod {
    TODAY, WEEK, MONTH;

    companion object {
        fun parse(value: String): LeaderboardPeriod =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: throw InvalidPeriodException()
    }
}

data class LeaderboardRow(val rank: Int, val card: PlayerCard, val distanceM: Long, val isMe: Boolean)

/** [gapM] is how far the Player is behind [ahead], the Player ranked just above them. */
data class Leaderboard(val rows: List<LeaderboardRow>, val ahead: PlayerCard?, val gapM: Long?)

/** UC-16 View Friend Leaderboard. Only the Player and their friends; no global board (BR-3). */
@Service
class LeaderboardService(
    private val gameCalendar: GameCalendar,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val friendService: FriendService,
    private val dailyStatsRepository: DailyStatsRepository,
) {
    fun leaderboard(userId: UUID, period: LeaderboardPeriod): Leaderboard {
        val today = gameCalendar.today()
        val from: LocalDate = when (period) {
            LeaderboardPeriod.TODAY -> today
            LeaderboardPeriod.WEEK -> gameCalendar.startOfWeek(today)
            LeaderboardPeriod.MONTH -> gameCalendar.startOfMonth(today)
        }
        val userIds = friendRepository.findFriendIds(userId) + userId
        val distances = dailyStatsRepository.sumDistanceByUser(userIds, from, today)
        val sorted = userIds.mapNotNull(userRepository::findById)
            .map { friendService.cardOf(it) to (distances[it.id] ?: 0L) }
            .sortedWith(compareByDescending<Pair<PlayerCard, Long>> { it.second }.thenBy { it.first.displayName.lowercase() })

        val rows = sorted.mapIndexed { index, (card, distanceM) ->
            // Players with the same distance share a rank.
            val rank = sorted.indexOfFirst { it.second == distanceM } + 1
            LeaderboardRow(rank, card, distanceM, isMe = card.userId == userId)
        }
        val myIndex = rows.indexOfFirst { it.isMe }
        val ahead = rows.take(myIndex).lastOrNull { it.distanceM > rows[myIndex].distanceM }
        return Leaderboard(rows, ahead?.card, ahead?.let { it.distanceM - rows[myIndex].distanceM })
    }
}

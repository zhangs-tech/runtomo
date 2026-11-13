package com.zhangs_tech.runtomo.server.home

import com.zhangs_tech.runtomo.server.activity.DailyStats
import com.zhangs_tech.runtomo.server.activity.DailyStatsRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.quest.QuestProgress
import com.zhangs_tech.runtomo.server.quest.QuestService
import com.zhangs_tech.runtomo.server.tomo.Mood
import com.zhangs_tech.runtomo.server.tomo.MoodRules
import com.zhangs_tech.runtomo.server.tomo.Tomo
import com.zhangs_tech.runtomo.server.tomo.TomoRepository
import com.zhangs_tech.runtomo.server.user.User
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

data class Home(
    val user: User,
    val tomo: Tomo?,
    val mood: Mood,
    val today: DailyStats,
    val quests: List<QuestProgress>,
    val lastSyncDate: LocalDate?,
)

/** UC-08. Everything the Home Screen needs, in one response. */
@Service
class HomeService(
    private val gameCalendar: GameCalendar,
    private val userRepository: UserRepository,
    private val tomoRepository: TomoRepository,
    private val dailyStatsRepository: DailyStatsRepository,
    private val moodRules: MoodRules,
    private val questService: QuestService,
) {
    fun getHome(userId: UUID): Home {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException()
        val today = gameCalendar.today()
        val todayStats = dailyStatsRepository.find(userId, today) ?: DailyStats(userId, today)
        return Home(
            user = user,
            tomo = user.equippedTomoId?.let(tomoRepository::findById),
            mood = moodRules.pick(todayStats.distanceM),
            today = todayStats,
            quests = questService.todayQuests(userId),
            lastSyncDate = dailyStatsRepository.findLatestDate(userId),
        )
    }
}

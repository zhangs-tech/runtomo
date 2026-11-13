package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.home.HomeService
import com.zhangs_tech.runtomo.server.food.FoodService
import com.zhangs_tech.runtomo.server.quest.QuestService
import com.zhangs_tech.runtomo.server.social.FriendService
import com.zhangs_tech.runtomo.server.social.LeaderboardPeriod
import com.zhangs_tech.runtomo.server.social.LeaderboardService
import com.zhangs_tech.runtomo.server.summon.SummonService
import com.zhangs_tech.runtomo.server.tomo.TomoService
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** Runs the core loop against PostgreSQL to check the SQL. Each test rolls back. */
@SpringBootTest(properties = ["runtomo.google.web-client-id=test-client-id"])
@Transactional
class ActivityDatabaseTests(
    @Autowired val userRepository: UserRepository,
    @Autowired val tomoService: TomoService,
    @Autowired val activityService: ActivityService,
    @Autowired val questService: QuestService,
    @Autowired val homeService: HomeService,
    @Autowired val gameCalendar: GameCalendar,
    @Autowired val historyService: HistoryService,
    @Autowired val summonService: SummonService,
    @Autowired val friendService: FriendService,
    @Autowired val leaderboardService: LeaderboardService,
    @Autowired val foodService: FoodService,
) {
    private fun createPlayer(): UUID {
        val unique = UUID.randomUUID().toString()
        val user = userRepository.create("test-$unique", "Runner0001", unique.take(8))!!
        tomoService.chooseStarter(user.id, "luppy", "Mochi")
        return user.id
    }

    @Test
    fun `sync, claim and home use the database correctly`() {
        val userId = createPlayer()
        val today = gameCalendar.today()

        activityService.sync(userId, today, steps = 1600)
        val second = activityService.sync(userId, today, steps = 2000)
        val repeated = activityService.sync(userId, today, steps = 2000)
        val balance = questService.claim(userId, "walk-1km")
        val home = homeService.getHome(userId)

        assertEquals(30, second.rewards!!.coins)
        assertEquals(null, repeated.rewards)
        assertEquals(250, balance)
        assertEquals(250, home.user.coinBalance)
        assertEquals(2000, home.today.steps)
        assertEquals(1500, home.tomo!!.distanceTogetherM)
        assertEquals(15, home.tomo.bondPoints)
        assertEquals(today, home.lastSyncDate)
    }

    @Test
    fun `history and summon use the database correctly`() {
        val userId = createPlayer()
        val today = gameCalendar.today()
        activityService.sync(userId, today, steps = 20_000) // 15 km pays 750 coins
        questService.claim(userId, "walk-1km")
        questService.claim(userId, "walk-3km") // 1050 coins in total

        val day = historyService.days(userId, cursor = null, limit = 30).days.single()
        val summon = summonService.summon(userId, "standard")
        val latest = summonService.latest(userId, 1).single()

        assertEquals(1050, day.coins)
        assertEquals(15_000, historyService.totals(userId).month.distanceM)
        assertEquals(summon.summon.id, latest.summon.id)
        assertEquals(if (summon.summon.wasDuplicate) 250L else 50L, summon.coinBalance)
        assertEquals(summon.coinBalance, latest.coinBalance)
    }

    @Test
    fun `friends, leaderboard and feeding use the database correctly`() {
        val kai = createPlayer()
        val ana = createPlayer()
        val today = gameCalendar.today()
        activityService.sync(ana, today, steps = 4000) // 3000 m pays 300 coins
        activityService.sync(kai, today, steps = 2000) // 1500 m pays 150 coins

        friendService.sendRequest(kai, ana)
        friendService.accept(ana, friendService.incomingRequests(ana).single().id)
        val board = leaderboardService.leaderboard(kai, LeaderboardPeriod.WEEK)
        foodService.buy(kai, "banana")
        val fed = foodService.feed(kai, userRepository.findById(kai)!!.equippedTomoId!!, "banana")

        assertEquals(listOf(ana, kai), board.rows.map { it.card.userId })
        assertEquals(1500L, board.gapM)
        assertEquals(3000L, friendService.friends(kai).single().todayM)
        assertEquals(15 + 3, fed.tomo.bondPoints)
        assertEquals(0, fed.pantry.stock.first { it.food.code == "banana" }.quantity)
        assertEquals(100L, fed.pantry.coinBalance)
    }
}

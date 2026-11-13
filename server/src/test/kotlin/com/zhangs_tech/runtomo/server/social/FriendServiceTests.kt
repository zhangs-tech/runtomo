package com.zhangs_tech.runtomo.server.social

import com.zhangs_tech.runtomo.server.activity.DailyStats
import com.zhangs_tech.runtomo.server.activity.InMemoryDailyStatsRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import com.zhangs_tech.runtomo.server.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class FriendServiceTests {
    private val userRepository = InMemoryUserRepository()
    private val tomoRepository = InMemoryTomoRepository()
    private val friends = InMemoryFriendRepository()
    private val requests = InMemoryFriendRequestRepository()
    private val dailyStats = InMemoryDailyStatsRepository()
    private val calendar = GameCalendar(testDaytimeClock)
    private val service = FriendService(testGameData, calendar, userRepository, tomoRepository, friends, requests, dailyStats)
    private val leaderboards = LeaderboardService(calendar, userRepository, friends, service, dailyStats)

    private val today = LocalDate.of(2026, 11, 14) // a Saturday
    private val kai = player("kai", "Kai")
    private val ana = player("ana", "Ana")
    private val bo = player("bo", "Bo")

    private fun player(code: String, name: String): User {
        val user = userRepository.create("sub-$code", name, code)!!
        val tomo = tomoRepository.create(user.id, "luppy", "$name's Tomo", today)
        userRepository.setEquippedTomo(user.id, tomo.id)
        return userRepository.findById(user.id)!!
    }

    private fun walk(user: User, date: LocalDate, distanceM: Int) =
        dailyStats.save(DailyStats(user.id, date, distanceM * 4 / 3, distanceM))

    @Test
    fun `look up shows the name and equipped Tomo`() {
        val card = service.lookUp(kai.id, " ana ")

        assertEquals("Ana", card.displayName)
        assertEquals("Ana's Tomo", card.tomo!!.nickname)
    }

    @Test
    fun `look up refuses an unknown code, the own code, and a friend`() {
        assertThrows<PlayerNotFoundException> { service.lookUp(kai.id, "nobody") }
        assertThrows<OwnFriendCodeException> { service.lookUp(kai.id, "kai") }
        friends.create(kai.id, ana.id)
        assertThrows<AlreadyFriendsException> { service.lookUp(kai.id, "ana") }
    }

    @Test
    fun `a request then an accept links both Players`() {
        assertEquals(RequestOutcome.SENT, service.sendRequest(kai.id, ana.id))
        val request = service.incomingRequests(ana.id).single()
        assertEquals("Kai", request.from.displayName)

        service.accept(ana.id, request.id)

        assertTrue(friends.areFriends(kai.id, ana.id))
        assertTrue(requests.requests.isEmpty())
    }

    @Test
    fun `a request back to a Player who already asked links both at once`() {
        service.sendRequest(kai.id, ana.id)

        assertEquals(RequestOutcome.NOW_FRIENDS, service.sendRequest(ana.id, kai.id))
        assertTrue(friends.areFriends(kai.id, ana.id))
        assertTrue(requests.requests.isEmpty())
    }

    @Test
    fun `decline deletes the request, and only the receiver can answer`() {
        service.sendRequest(kai.id, ana.id)
        val request = service.incomingRequests(ana.id).single()

        assertThrows<FriendRequestNotFoundException> { service.accept(kai.id, request.id) }
        service.decline(ana.id, request.id)

        assertTrue(requests.requests.isEmpty())
    }

    @Test
    fun `the friend limit applies on send and on accept`() {
        val cy = player("cy", "Cy")
        friends.create(kai.id, bo.id)
        friends.create(kai.id, cy.id) // the test limit is 2

        assertThrows<FriendLimitException> { service.sendRequest(kai.id, ana.id) }
        service.sendRequest(ana.id, kai.id)
        assertThrows<FriendLimitException> { service.accept(kai.id, service.incomingRequests(kai.id).single().id) }
    }

    @Test
    fun `friends shows today, week and month distance`() {
        friends.create(kai.id, ana.id)
        walk(ana, today, 1000)
        walk(ana, LocalDate.of(2026, 11, 9), 2000) // Monday this week
        walk(ana, LocalDate.of(2026, 11, 2), 4000) // last week

        val friend = service.friends(kai.id).single()

        assertEquals(1000, friend.todayM)
        assertEquals(3000, friend.weekM)
        assertEquals(7000, friend.monthM)
    }

    @Test
    fun `remove deletes the link for both Players`() {
        friends.create(kai.id, ana.id)

        service.remove(kai.id, ana.id)

        assertTrue(service.friends(ana.id).isEmpty())
    }

    @Test
    fun `leaderboard ranks the Player and friends with the gap to the next`() {
        friends.create(kai.id, ana.id)
        friends.create(kai.id, bo.id)
        walk(ana, today, 3000)
        walk(kai, today, 1200)
        walk(bo, LocalDate.of(2026, 11, 2), 9000) // not this week

        val board = leaderboards.leaderboard(kai.id, LeaderboardPeriod.WEEK)

        assertEquals(listOf("Ana" to 1, "Kai" to 2, "Bo" to 3), board.rows.map { it.card.displayName to it.rank })
        assertTrue(board.rows[1].isMe)
        assertEquals("Ana", board.ahead!!.displayName)
        assertEquals(1800, board.gapM)
    }

    @Test
    fun `leaderboard gives equal distances the same rank, and the leader has no gap`() {
        friends.create(kai.id, ana.id)
        walk(ana, today, 1000)
        walk(kai, today, 1000)

        val board = leaderboards.leaderboard(kai.id, LeaderboardPeriod.TODAY)

        assertEquals(listOf(1, 1), board.rows.map { it.rank })
        assertEquals(null, board.ahead)
        assertEquals(null, board.gapM)
    }
}

package com.zhangs_tech.runtomo.server.social

import com.zhangs_tech.runtomo.server.activity.DailyStatsRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.tomo.Tomo
import com.zhangs_tech.runtomo.server.tomo.TomoRepository
import com.zhangs_tech.runtomo.server.user.User
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** What other Players can see (UC-15 BR-2): the display name and the equipped Tomo. */
data class PlayerCard(val userId: UUID, val displayName: String, val tomo: Tomo?)

data class IncomingRequest(val id: UUID, val from: PlayerCard)

data class FriendSummary(val card: PlayerCard, val todayM: Long, val weekM: Long, val monthM: Long)

enum class RequestOutcome { SENT, NOW_FRIENDS }

/** UC-15 Add Friend. */
@Service
class FriendService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val userRepository: UserRepository,
    private val tomoRepository: TomoRepository,
    private val friendRepository: FriendRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val dailyStatsRepository: DailyStatsRepository,
) {
    /** UC-15 step 3, with 3a and 3b. */
    fun lookUp(userId: UUID, friendCode: String): PlayerCard {
        val other = userRepository.findByFriendCode(friendCode.trim()) ?: throw PlayerNotFoundException()
        if (other.id == userId) throw OwnFriendCodeException()
        if (friendRepository.areFriends(userId, other.id)) throw AlreadyFriendsException()
        return cardOf(other)
    }

    @Transactional
    fun sendRequest(userId: UUID, toUserId: UUID): RequestOutcome {
        if (toUserId == userId) throw OwnFriendCodeException()
        lockBoth(userId, toUserId)
        if (friendRepository.areFriends(userId, toUserId)) throw AlreadyFriendsException()
        checkLimit(userId)

        // 4a: a request back to a Player who already asked links both Players at once.
        val reverse = friendRequestRepository.find(toUserId, userId)
        if (reverse != null) {
            checkLimit(toUserId)
            friendRepository.create(userId, toUserId)
            friendRequestRepository.delete(reverse.id)
            return RequestOutcome.NOW_FRIENDS
        }
        friendRequestRepository.create(userId, toUserId)
        return RequestOutcome.SENT
    }

    fun incomingRequests(userId: UUID): List<IncomingRequest> =
        friendRequestRepository.findIncoming(userId).mapNotNull { request ->
            userRepository.findById(request.fromUserId)?.let { IncomingRequest(request.id, cardOf(it)) }
        }

    @Transactional
    fun accept(userId: UUID, requestId: UUID) {
        val request = incomingRequest(userId, requestId)
        lockBoth(request.fromUserId, userId)
        checkLimit(userId)
        checkLimit(request.fromUserId)
        friendRepository.create(request.fromUserId, userId)
        friendRequestRepository.delete(request.id)
    }

    /** 6a: the sender is not told. */
    fun decline(userId: UUID, requestId: UUID) {
        friendRequestRepository.delete(incomingRequest(userId, requestId).id)
    }

    fun friends(userId: UUID): List<FriendSummary> {
        val friendIds = friendRepository.findFriendIds(userId)
        val today = gameCalendar.today()
        val todayM = dailyStatsRepository.sumDistanceByUser(friendIds, today, today)
        val weekM = dailyStatsRepository.sumDistanceByUser(friendIds, gameCalendar.startOfWeek(today), today)
        val monthM = dailyStatsRepository.sumDistanceByUser(friendIds, gameCalendar.startOfMonth(today), today)
        return friendIds.mapNotNull(userRepository::findById)
            .map { FriendSummary(cardOf(it), todayM[it.id] ?: 0, weekM[it.id] ?: 0, monthM[it.id] ?: 0) }
            .sortedBy { it.card.displayName.lowercase() }
    }

    /** The link is removed for both Players, and neither is told. */
    fun remove(userId: UUID, friendId: UUID) {
        friendRepository.delete(userId, friendId)
    }

    fun cardOf(user: User): PlayerCard =
        PlayerCard(user.id, user.displayName, user.equippedTomoId?.let(tomoRepository::findById))

    private fun incomingRequest(userId: UUID, requestId: UUID): FriendRequest =
        friendRequestRepository.findById(requestId)?.takeIf { it.toUserId == userId }
            ?: throw FriendRequestNotFoundException()

    private fun checkLimit(userId: UUID) {
        val max = gameData.tunables.maxFriends
        if (friendRepository.count(userId) >= max) throw FriendLimitException(max)
    }

    // Both rows in a fixed order, so two requests between the same Players cannot deadlock.
    private fun lockBoth(userId: UUID, otherUserId: UUID) {
        listOf(userId, otherUserId).sortedBy { it.toString() }.forEach {
            userRepository.lockById(it) ?: if (it == userId) throw UserNotFoundException() else throw PlayerNotFoundException()
        }
    }
}

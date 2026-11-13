package com.zhangs_tech.runtomo.social

import com.zhangs_tech.runtomo.tomo.Species
import kotlinx.serialization.Serializable

/** UC-15 BR-2: friends see only the Tomo's nickname and species. */
@Serializable
data class DisplayedTomo(val nickname: String, val species: Species)

@Serializable
data class PlayerCard(val userId: String, val displayName: String, val tomo: DisplayedTomo? = null)

@Serializable
data class IncomingRequest(val id: String, val from: PlayerCard)

@Serializable
data class Friend(val player: PlayerCard, val todayM: Long, val weekM: Long, val monthM: Long)

enum class LeaderboardPeriod(val param: String) { TODAY("today"), WEEK("week"), MONTH("month") }

@Serializable
data class LeaderboardRow(val rank: Int, val player: PlayerCard, val distanceM: Long, val isMe: Boolean)

@Serializable
data class Leaderboard(
    val rows: List<LeaderboardRow>,
    /** The Player ranked just above the Player, or null if the Player leads. */
    val ahead: PlayerCard? = null,
    val gapM: Long? = null,
)

sealed interface LookUpResult {
    data class Found(val player: PlayerCard) : LookUpResult
    data object NotFound : LookUpResult
    data object OwnCode : LookUpResult
    data object AlreadyFriends : LookUpResult
}

enum class SendResult { SENT, NOW_FRIENDS, ALREADY_FRIENDS, LIMIT_REACHED }

/** The RunTomo server social endpoints (UC-15, UC-16). Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface SocialApi {
    suspend fun lookUp(friendCode: String): LookUpResult
    suspend fun sendRequest(toUserId: String): SendResult
    suspend fun incomingRequests(): List<IncomingRequest>

    /** Returns false if one of the Players reached the friend limit. */
    suspend fun accept(requestId: String): Boolean
    suspend fun decline(requestId: String)
    suspend fun friends(): List<Friend>
    suspend fun remove(friendUserId: String)
    suspend fun leaderboard(period: LeaderboardPeriod): Leaderboard
}

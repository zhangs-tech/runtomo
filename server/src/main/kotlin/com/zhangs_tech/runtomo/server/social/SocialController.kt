package com.zhangs_tech.runtomo.server.social

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.social.dto.FriendResponse
import com.zhangs_tech.runtomo.server.social.dto.IncomingRequestResponse
import com.zhangs_tech.runtomo.server.social.dto.LeaderboardResponse
import com.zhangs_tech.runtomo.server.social.dto.LeaderboardRowResponse
import com.zhangs_tech.runtomo.server.social.dto.PlayerCardResponse
import com.zhangs_tech.runtomo.server.social.dto.SendFriendRequest
import com.zhangs_tech.runtomo.server.social.dto.SendFriendRequestResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SocialController(
    private val friendService: FriendService,
    private val leaderboardService: LeaderboardService,
    private val gameData: GameData,
) {
    @GetMapping("/users/by-code/{code}")
    fun lookUp(@CurrentUserId userId: UUID, @PathVariable code: String): PlayerCardResponse =
        PlayerCardResponse.from(friendService.lookUp(userId, code), gameData)

    @PostMapping("/friend-requests")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendRequest(@CurrentUserId userId: UUID, @RequestBody request: SendFriendRequest): SendFriendRequestResponse =
        SendFriendRequestResponse(friendService.sendRequest(userId, request.toUserId).name)

    @GetMapping("/friend-requests")
    fun getIncoming(@CurrentUserId userId: UUID): List<IncomingRequestResponse> =
        friendService.incomingRequests(userId).map { IncomingRequestResponse(it.id, PlayerCardResponse.from(it.from, gameData)) }

    @PostMapping("/friend-requests/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun accept(@CurrentUserId userId: UUID, @PathVariable id: UUID) = friendService.accept(userId, id)

    @PostMapping("/friend-requests/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun decline(@CurrentUserId userId: UUID, @PathVariable id: UUID) = friendService.decline(userId, id)

    @GetMapping("/friends")
    fun getFriends(@CurrentUserId userId: UUID): List<FriendResponse> =
        friendService.friends(userId).map {
            FriendResponse(PlayerCardResponse.from(it.card, gameData), it.todayM, it.weekM, it.monthM)
        }

    @DeleteMapping("/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@CurrentUserId userId: UUID, @PathVariable friendId: UUID) = friendService.remove(userId, friendId)

    @GetMapping("/leaderboard")
    fun getLeaderboard(
        @CurrentUserId userId: UUID,
        @RequestParam(defaultValue = "week") period: String,
    ): LeaderboardResponse {
        val board = leaderboardService.leaderboard(userId, LeaderboardPeriod.parse(period))
        return LeaderboardResponse(
            period = period.lowercase(),
            rows = board.rows.map { LeaderboardRowResponse(it.rank, PlayerCardResponse.from(it.card, gameData), it.distanceM, it.isMe) },
            ahead = board.ahead?.let { PlayerCardResponse.from(it, gameData) },
            gapM = board.gapM,
        )
    }
}

package com.zhangs_tech.runtomo.server.social.dto

import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.social.PlayerCard
import com.zhangs_tech.runtomo.server.tomo.dto.SpeciesResponse
import java.util.UUID

/** UC-15 BR-2: friends see the Tomo's nickname and species, not its Bond or history. */
data class DisplayedTomoResponse(val nickname: String, val species: SpeciesResponse)

data class PlayerCardResponse(val userId: UUID, val displayName: String, val tomo: DisplayedTomoResponse?) {
    companion object {
        fun from(card: PlayerCard, gameData: GameData) = PlayerCardResponse(
            userId = card.userId,
            displayName = card.displayName,
            tomo = card.tomo?.let { DisplayedTomoResponse(it.nickname, SpeciesResponse.from(gameData.findSpecies(it.speciesCode)!!)) },
        )
    }
}

data class SendFriendRequest(val toUserId: UUID)

data class SendFriendRequestResponse(val status: String)

data class IncomingRequestResponse(val id: UUID, val from: PlayerCardResponse)

data class FriendResponse(val player: PlayerCardResponse, val todayM: Long, val weekM: Long, val monthM: Long)

data class LeaderboardRowResponse(val rank: Int, val player: PlayerCardResponse, val distanceM: Long, val isMe: Boolean)

data class LeaderboardResponse(
    val period: String,
    val rows: List<LeaderboardRowResponse>,
    /** The Player ranked just above the Player, or null if the Player is first. */
    val ahead: PlayerCardResponse?,
    val gapM: Long?,
)

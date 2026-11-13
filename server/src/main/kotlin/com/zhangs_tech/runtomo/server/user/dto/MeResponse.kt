package com.zhangs_tech.runtomo.server.user.dto

import com.zhangs_tech.runtomo.server.user.User
import java.util.UUID

data class MeResponse(
    val id: UUID,
    val displayName: String,
    val friendCode: String,
    val coinBalance: Long,
    val equippedTomoId: UUID?,
) {
    companion object {
        fun from(user: User) = MeResponse(
            id = user.id,
            displayName = user.displayName,
            friendCode = user.friendCode,
            coinBalance = user.coinBalance,
            equippedTomoId = user.equippedTomoId,
        )
    }
}

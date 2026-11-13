package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.game.Rarity
import java.util.UUID

interface SummonRepository {
    fun create(
        userId: UUID,
        bannerCode: String,
        speciesCode: String,
        rarity: Rarity,
        costCoins: Int,
        wasPity: Boolean,
        userTomoId: UUID?,
    ): Summon

    /** Newest first. */
    fun findLatest(userId: UUID, limit: Int): List<Summon>
}

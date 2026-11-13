package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.game.Rarity
import java.time.Instant
import java.util.UUID

/** A saved summon. [rarity] and [costCoins] are copied at summon time, so history stays correct. */
data class Summon(
    val id: UUID,
    val userId: UUID,
    val bannerCode: String,
    val speciesCode: String,
    val rarity: Rarity,
    val costCoins: Int,
    val wasPity: Boolean,
    val wasDuplicate: Boolean,
    val userTomoId: UUID?,
    val createdAt: Instant,
)

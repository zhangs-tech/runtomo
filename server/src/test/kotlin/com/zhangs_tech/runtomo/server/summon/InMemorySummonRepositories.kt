package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.game.Rarity
import java.time.Instant
import java.util.UUID

class InMemorySummonRepository : SummonRepository {
    val summons = mutableListOf<Summon>()

    override fun create(
        userId: UUID,
        bannerCode: String,
        speciesCode: String,
        rarity: Rarity,
        costCoins: Int,
        wasPity: Boolean,
        userTomoId: UUID?,
    ): Summon {
        val summon = Summon(
            UUID.randomUUID(), userId, bannerCode, speciesCode, rarity, costCoins, wasPity,
            wasDuplicate = userTomoId == null, userTomoId = userTomoId, createdAt = Instant.now(),
        )
        summons += summon
        return summon
    }

    override fun findLatest(userId: UUID, limit: Int): List<Summon> =
        summons.filter { it.userId == userId }.reversed().take(limit)
}

class InMemoryPityRepository : PityRepository {
    val pulls = mutableMapOf<Pair<UUID, String>, Int>()

    override fun pullsSinceEpic(userId: UUID, bannerCode: String): Int = pulls[userId to bannerCode] ?: 0

    override fun save(userId: UUID, bannerCode: String, pullsSinceEpic: Int) {
        pulls[userId to bannerCode] = pullsSinceEpic
    }
}

/** Returns the given rolls in order. */
class FixedRandom(vararg rolls: Int) : java.util.random.RandomGenerator {
    private val rolls = ArrayDeque(rolls.toList())
    val bounds = mutableListOf<Int>()

    override fun nextLong(): Long = error("Not used")

    override fun nextInt(bound: Int): Int {
        bounds += bound
        return rolls.removeFirst()
    }
}

package com.zhangs_tech.runtomo.server.summon

import java.util.UUID

interface PityRepository {
    /** Summons since the last Epic or higher result on this banner, 0 if none yet. */
    fun pullsSinceEpic(userId: UUID, bannerCode: String): Int
    fun save(userId: UUID, bannerCode: String, pullsSinceEpic: Int)
}

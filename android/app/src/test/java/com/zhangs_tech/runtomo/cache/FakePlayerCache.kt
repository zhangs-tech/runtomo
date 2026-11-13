package com.zhangs_tech.runtomo.cache

import com.zhangs_tech.runtomo.activity.Rewards
import com.zhangs_tech.runtomo.home.HomeData

class FakePlayerCache : PlayerCache {
    var acceptedSync: AcceptedSync? = null
    var pending: Rewards? = null
    var cachedHome: HomeData? = null

    override suspend fun lastAcceptedSync(): AcceptedSync? = acceptedSync
    override suspend fun saveAcceptedSync(sync: AcceptedSync) {
        acceptedSync = sync
    }

    override suspend fun pendingRewards(): Rewards? = pending
    override suspend fun addPendingRewards(rewards: Rewards) {
        pending = pending?.plus(rewards) ?: rewards
    }
    override suspend fun clearPendingRewards() {
        pending = null
    }

    override suspend fun home(): HomeData? = cachedHome
    override suspend fun saveHome(home: HomeData) {
        cachedHome = home
    }

    override suspend fun clear() {
        acceptedSync = null
        pending = null
        cachedHome = null
    }
}

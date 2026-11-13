package com.zhangs_tech.runtomo.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zhangs_tech.runtomo.activity.Rewards
import com.zhangs_tech.runtomo.home.HomeData
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DataStorePlayerCache @Inject constructor(
    @PlayerDataStore private val dataStore: DataStore<Preferences>,
) : PlayerCache {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun lastAcceptedSync(): AcceptedSync? {
        val preferences = dataStore.data.first()
        return AcceptedSync(
            date = preferences[SYNC_DATE] ?: return null,
            steps = preferences[SYNC_STEPS] ?: return null,
            strideM = preferences[SYNC_STRIDE_M] ?: return null,
        )
    }

    override suspend fun saveAcceptedSync(sync: AcceptedSync) {
        dataStore.edit {
            it[SYNC_DATE] = sync.date
            it[SYNC_STEPS] = sync.steps
            it[SYNC_STRIDE_M] = sync.strideM
        }
    }

    override suspend fun pendingRewards(): Rewards? = read(PENDING_REWARDS)

    override suspend fun addPendingRewards(rewards: Rewards) {
        dataStore.edit { preferences ->
            val pending = preferences[PENDING_REWARDS]?.let { decode<Rewards>(it) }
            preferences[PENDING_REWARDS] = json.encodeToString(pending?.plus(rewards) ?: rewards)
        }
    }

    override suspend fun clearPendingRewards() {
        dataStore.edit { it.remove(PENDING_REWARDS) }
    }

    override suspend fun home(): HomeData? = read(HOME)

    override suspend fun saveHome(home: HomeData) {
        dataStore.edit { it[HOME] = json.encodeToString(home) }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private suspend inline fun <reified T> read(key: Preferences.Key<String>): T? =
        dataStore.data.first()[key]?.let { decode(it) }

    // A cache written by an older app version may not decode. Then it counts as empty.
    private inline fun <reified T> decode(value: String): T? =
        try {
            json.decodeFromString<T>(value)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    companion object {
        /** Also listed in res/xml/data_extraction_rules.xml, which excludes it from backup. */
        const val FILE_NAME = "player_cache"

        private val SYNC_DATE = stringPreferencesKey("sync_date")
        private val SYNC_STEPS = intPreferencesKey("sync_steps")
        private val SYNC_STRIDE_M = doublePreferencesKey("sync_stride_m")
        private val PENDING_REWARDS = stringPreferencesKey("pending_rewards")
        private val HOME = stringPreferencesKey("home")
    }
}

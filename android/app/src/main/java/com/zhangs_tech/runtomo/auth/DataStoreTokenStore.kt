package com.zhangs_tech.runtomo.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DataStoreTokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TokenStore {

    override suspend fun read(): AuthTokens? {
        val preferences = dataStore.data.first()
        val accessToken = preferences[ACCESS_TOKEN] ?: return null
        val refreshToken = preferences[REFRESH_TOKEN] ?: return null
        return AuthTokens(accessToken, refreshToken)
    }

    override suspend fun save(tokens: AuthTokens) {
        dataStore.edit {
            it[ACCESS_TOKEN] = tokens.accessToken
            it[REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        /** Also listed in res/xml/data_extraction_rules.xml, which excludes it from backup. */
        const val FILE_NAME = "auth_tokens"

        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}

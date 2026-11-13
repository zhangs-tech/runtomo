package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.cache.PlayerCache
import com.zhangs_tech.runtomo.network.AccessTokenProvider
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.user.UserApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val userApi: UserApi,
    private val playerCache: PlayerCache,
) : AccessTokenProvider {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // The server revokes a refresh token when it is used, so only one refresh may run at a time.
    private val refreshMutex = Mutex()

    /**
     * Refreshes the stored session. If the server cannot be reached, the Player stays signed in
     * with the stored tokens. If the server rejects the refresh token, the Player is signed out.
     */
    suspend fun restoreSession() {
        val tokens = tokenStore.read() ?: return endSession()
        try {
            refreshMutex.withLock { refresh(tokens) }
            _sessionState.value = sessionStateForUser()
        } catch (e: ApiException.Unauthorized) {
            endSession()
        } catch (e: ApiException) {
            _sessionState.value = SessionState.Ready
        }
    }

    /** @throws ApiException if the server rejects [idToken] or cannot be reached. */
    suspend fun signIn(idToken: String) {
        val result = authApi.signIn(idToken)
        tokenStore.save(result.tokens)
        _sessionState.value = if (result.isNewUser) SessionState.NeedsDisplayName else sessionStateForUser()
    }

    fun onDisplayNameConfirmed() {
        _sessionState.value = SessionState.NeedsStarterTomo
    }

    /** The Player has no Tomo, for example after an offline start (UC-02 7a). */
    fun onStarterTomoMissing() {
        _sessionState.value = SessionState.NeedsStarterTomo
    }

    fun onStarterTomoChosen() {
        _sessionState.value = SessionState.Ready
    }

    /** Signs out on this device, even if the server cannot revoke the refresh token. */
    suspend fun signOut() {
        tokenStore.read()?.let { tokens ->
            try {
                authApi.signOut(tokens.refreshToken)
            } catch (e: ApiException) {
                // The refresh token expires on the server by itself.
            }
        }
        endSession()
    }

    override suspend fun accessToken(): String? = tokenStore.read()?.accessToken

    override suspend fun refreshAccessToken(staleAccessToken: String?): String? = refreshMutex.withLock {
        val tokens = tokenStore.read() ?: return null
        // Another request already refreshed while this one waited for the lock.
        if (tokens.accessToken != staleAccessToken) return tokens.accessToken
        try {
            refresh(tokens).accessToken
        } catch (e: ApiException.Unauthorized) {
            endSession()
            null
        } catch (e: ApiException) {
            null
        }
    }

    private suspend fun refresh(tokens: AuthTokens): AuthTokens =
        authApi.refresh(tokens.refreshToken).also { tokenStore.save(it) }

    /** A returning Player without a Tomo still has to choose a starter (UC-02 7a). */
    private suspend fun sessionStateForUser(): SessionState =
        try {
            if (userApi.getMe().hasTomo) SessionState.Ready else SessionState.NeedsStarterTomo
        } catch (e: ApiException.Unauthorized) {
            throw e
        } catch (e: ApiException) {
            SessionState.Ready
        }

    private suspend fun endSession() {
        tokenStore.clear()
        playerCache.clear()
        _sessionState.value = SessionState.SignedOut
    }
}

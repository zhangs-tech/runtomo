package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /**
     * Refreshes the stored session. If the server cannot be reached, the Player stays signed in
     * with the stored tokens. If the server rejects the refresh token, the Player is signed out.
     */
    suspend fun restoreSession() {
        val tokens = tokenStore.read()
        if (tokens == null) {
            _sessionState.value = SessionState.SignedOut
            return
        }
        _sessionState.value = try {
            tokenStore.save(authApi.refresh(tokens.refreshToken))
            SessionState.Ready
        } catch (e: ApiException.Unauthorized) {
            tokenStore.clear()
            SessionState.SignedOut
        } catch (e: ApiException) {
            SessionState.Ready
        }
    }

    /** @throws ApiException if the server rejects [idToken] or cannot be reached. */
    suspend fun signIn(idToken: String) {
        val result = authApi.signIn(idToken)
        tokenStore.save(result.tokens)
        _sessionState.value = if (result.isNewUser) SessionState.NeedsStarterTomo else SessionState.Ready
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
        tokenStore.clear()
        _sessionState.value = SessionState.SignedOut
    }
}

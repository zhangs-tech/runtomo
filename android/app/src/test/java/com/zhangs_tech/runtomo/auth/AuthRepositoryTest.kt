package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.network.ApiException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class AuthRepositoryTest {

    private val authApi = FakeAuthApi()
    private val tokenStore = FakeTokenStore()
    private val repository = AuthRepository(authApi, tokenStore)

    @Test
    fun initialState_isLoading() {
        assertEquals(SessionState.Loading, repository.sessionState.value)
    }

    @Test
    fun restoreSession_withoutTokens_signsOut() = runTest {
        repository.restoreSession()

        assertEquals(SessionState.SignedOut, repository.sessionState.value)
    }

    @Test
    fun restoreSession_withValidRefreshToken_savesRotatedTokens() = runTest {
        tokenStore.tokens = AuthTokens("old-access", "old-refresh")

        repository.restoreSession()

        assertEquals(SessionState.Ready, repository.sessionState.value)
        assertEquals(AuthTokens("new-access", "new-refresh"), tokenStore.tokens)
    }

    @Test
    fun restoreSession_withRejectedRefreshToken_clearsTokensAndSignsOut() = runTest {
        tokenStore.tokens = AuthTokens("old-access", "old-refresh")
        authApi.refreshResult = Result.failure(ApiException.Unauthorized())

        repository.restoreSession()

        assertEquals(SessionState.SignedOut, repository.sessionState.value)
        assertNull(tokenStore.tokens)
    }

    @Test
    fun restoreSession_whenOffline_keepsTokensAndStaysSignedIn() = runTest {
        val stored = AuthTokens("old-access", "old-refresh")
        tokenStore.tokens = stored
        authApi.refreshResult = Result.failure(ApiException.Network(IOException()))

        repository.restoreSession()

        assertEquals(SessionState.Ready, repository.sessionState.value)
        assertEquals(stored, tokenStore.tokens)
    }

    @Test
    fun signIn_newUser_savesTokensAndNeedsStarterTomo() = runTest {
        authApi.signInResult = Result.success(SignInResult(AuthTokens("a", "r"), isNewUser = true))

        repository.signIn("id-token")

        assertEquals(SessionState.NeedsStarterTomo, repository.sessionState.value)
        assertEquals(AuthTokens("a", "r"), tokenStore.tokens)
    }

    @Test
    fun signIn_returningUser_isReady() = runTest {
        repository.signIn("id-token")

        assertEquals(SessionState.Ready, repository.sessionState.value)
    }

    @Test(expected = ApiException.Unauthorized::class)
    fun signIn_rejectedIdToken_throwsAndSavesNothing() = runTest {
        authApi.signInResult = Result.failure(ApiException.Unauthorized())
        try {
            repository.signIn("bad-token")
        } finally {
            assertNull(tokenStore.tokens)
        }
    }

    @Test
    fun signOut_revokesRefreshTokenAndClearsTokens() = runTest {
        tokenStore.tokens = AuthTokens("a", "r")

        repository.signOut()

        assertEquals(listOf("r"), authApi.signedOutRefreshTokens)
        assertNull(tokenStore.tokens)
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
    }

    @Test
    fun signOut_whenOffline_stillSignsOutLocally() = runTest {
        tokenStore.tokens = AuthTokens("a", "r")
        authApi.signOutError = ApiException.Network(IOException())

        repository.signOut()

        assertNull(tokenStore.tokens)
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
    }
}

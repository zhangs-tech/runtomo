package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.home.testHome
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.user.FakeUserApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class AuthRepositoryTest {

    private val authApi = FakeAuthApi()
    private val tokenStore = FakeTokenStore()
    private val userApi = FakeUserApi()
    private val playerCache = FakePlayerCache()
    private val repository = AuthRepository(authApi, tokenStore, userApi, playerCache)

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
    fun restoreSession_playerWithoutTomo_needsStarterTomo() = runTest {
        tokenStore.tokens = AuthTokens("old-access", "old-refresh")
        userApi.me = userApi.me.copy(hasTomo = false)

        repository.restoreSession()

        assertEquals(SessionState.NeedsStarterTomo, repository.sessionState.value)
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
    fun signIn_newUser_savesTokensAndNeedsDisplayName() = runTest {
        authApi.signInResult = Result.success(SignInResult(AuthTokens("a", "r"), isNewUser = true))

        repository.signIn("id-token")

        assertEquals(SessionState.NeedsDisplayName, repository.sessionState.value)
        assertEquals(AuthTokens("a", "r"), tokenStore.tokens)
    }

    @Test
    fun signIn_returningUser_isReady() = runTest {
        repository.signIn("id-token")

        assertEquals(SessionState.Ready, repository.sessionState.value)
    }

    @Test
    fun signIn_returningUserWithoutTomo_needsStarterTomo() = runTest {
        userApi.me = userApi.me.copy(hasTomo = false)

        repository.signIn("id-token")

        assertEquals(SessionState.NeedsStarterTomo, repository.sessionState.value)
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
    fun onboardingSteps_moveToStarterThenReady() {
        repository.onDisplayNameConfirmed()
        assertEquals(SessionState.NeedsStarterTomo, repository.sessionState.value)

        repository.onStarterTomoChosen()
        assertEquals(SessionState.Ready, repository.sessionState.value)
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

    @Test
    fun refreshAccessToken_staleToken_refreshesAndSaves() = runTest {
        tokenStore.tokens = AuthTokens("old-access", "old-refresh")

        assertEquals("new-access", repository.refreshAccessToken("old-access"))
        assertEquals(AuthTokens("new-access", "new-refresh"), tokenStore.tokens)
    }

    @Test
    fun refreshAccessToken_alreadyRefreshed_returnsStoredTokenWithoutRefresh() = runTest {
        tokenStore.tokens = AuthTokens("newer-access", "newer-refresh")
        authApi.refreshResult = Result.failure(AssertionError("refresh must not run"))

        assertEquals("newer-access", repository.refreshAccessToken("old-access"))
    }

    @Test
    fun refreshAccessToken_rejected_signsOut() = runTest {
        tokenStore.tokens = AuthTokens("old-access", "old-refresh")
        authApi.refreshResult = Result.failure(ApiException.Unauthorized())

        assertNull(repository.refreshAccessToken("old-access"))
        assertNull(tokenStore.tokens)
        assertEquals(SessionState.SignedOut, repository.sessionState.value)
    }

    @Test
    fun refreshAccessToken_offline_keepsSession() = runTest {
        val stored = AuthTokens("old-access", "old-refresh")
        tokenStore.tokens = stored
        authApi.refreshResult = Result.failure(ApiException.Network(IOException()))

        assertNull(repository.refreshAccessToken("old-access"))
        assertEquals(stored, tokenStore.tokens)
    }

    @Test
    fun signOut_clearsPlayerCache() = runTest {
        tokenStore.tokens = AuthTokens("a", "r")
        playerCache.cachedHome = testHome()

        repository.signOut()

        assertNull(playerCache.cachedHome)
    }
}

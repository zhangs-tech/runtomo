package com.zhangs_tech.runtomo.auth

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.user.FakeUserApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WelcomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val identityProvider = FakeIdentityProvider()
    private val authApi = FakeAuthApi()
    private val authRepository = AuthRepository(authApi, FakeTokenStore(), FakeUserApi())
    private val viewModel = WelcomeViewModel(identityProvider, authRepository)

    private fun signIn() = viewModel.signIn(ApplicationProvider.getApplicationContext())

    @Test
    fun signIn_success_signsInWithoutError() {
        authApi.signInResult = Result.success(SignInResult(AuthTokens("a", "r"), isNewUser = true))

        signIn()

        assertEquals(WelcomeUiState(), viewModel.uiState.value)
        assertEquals(SessionState.NeedsDisplayName, authRepository.sessionState.value)
    }

    @Test
    fun signIn_cancelled_returnsToIdle() {
        identityProvider.result = IdTokenResult.Cancelled

        signIn()

        assertEquals(WelcomeUiState(), viewModel.uiState.value)
        assertEquals(SessionState.Loading, authRepository.sessionState.value)
    }

    @Test
    fun signIn_noGoogleAccount_showsNoGoogleAccount() {
        identityProvider.result = IdTokenResult.NoAccount

        signIn()

        assertEquals(SignInError.NoGoogleAccount, viewModel.uiState.value.error)
    }

    @Test
    fun signIn_googleFailure_showsSignInFailed() {
        identityProvider.result = IdTokenResult.Failed(IllegalStateException())

        signIn()

        assertEquals(SignInError.SignInFailed, viewModel.uiState.value.error)
    }

    @Test
    fun signIn_offline_showsNoConnection() {
        authApi.signInResult = Result.failure(ApiException.Network(IOException()))

        signIn()

        assertEquals(SignInError.NoConnection, viewModel.uiState.value.error)
    }

    @Test
    fun signIn_serverRejectsIdToken_showsSignInFailed() {
        authApi.signInResult = Result.failure(ApiException.Unauthorized())

        signIn()

        assertEquals(SignInError.SignInFailed, viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isSigningIn)
    }
}

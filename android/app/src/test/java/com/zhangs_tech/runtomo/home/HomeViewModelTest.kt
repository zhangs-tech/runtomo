package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.auth.AuthTokens
import com.zhangs_tech.runtomo.auth.FakeAuthApi
import com.zhangs_tech.runtomo.auth.FakeTokenStore
import com.zhangs_tech.runtomo.auth.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun signOut_signsOutAndClearsTokens() {
        val tokenStore = FakeTokenStore(AuthTokens("a", "r"))
        val authRepository = AuthRepository(FakeAuthApi(), tokenStore)

        HomeViewModel(authRepository).signOut()

        assertEquals(SessionState.SignedOut, authRepository.sessionState.value)
        assertNull(tokenStore.tokens)
    }
}

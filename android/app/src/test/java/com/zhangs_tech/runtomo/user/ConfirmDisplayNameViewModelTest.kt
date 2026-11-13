package com.zhangs_tech.runtomo.user

import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.auth.FakeAuthApi
import com.zhangs_tech.runtomo.auth.FakeTokenStore
import com.zhangs_tech.runtomo.auth.SessionState
import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.network.ApiException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ConfirmDisplayNameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userApi = FakeUserApi()
    private val authRepository = AuthRepository(FakeAuthApi(), FakeTokenStore(), userApi, FakePlayerCache())
    private val viewModel by lazy { ConfirmDisplayNameViewModel(userApi, authRepository) }

    @Test
    fun init_showsNameFromGoogleProfile() {
        assertEquals(ConfirmDisplayNameUiState(displayName = "Kai Tan", isLoading = false), viewModel.uiState.value)
    }

    @Test
    fun confirm_savesTrimmedNameAndMovesToStarter() {
        viewModel.onDisplayNameChange("  Kai  ")

        viewModel.confirm()

        assertEquals(listOf("Kai"), userApi.savedDisplayNames)
        assertEquals(SessionState.NeedsStarterTomo, authRepository.sessionState.value)
    }

    @Test
    fun confirm_tooShort_showsRuleAndSavesNothing() {
        viewModel.onDisplayNameChange("Al")

        viewModel.confirm()

        assertEquals(DisplayNameError.Invalid, viewModel.uiState.value.error)
        assertEquals(emptyList<String>(), userApi.savedDisplayNames)
    }

    @Test
    fun confirm_offline_showsNoConnection() {
        viewModel.uiState.value
        userApi.error = ApiException.Network(IOException())

        viewModel.confirm()

        assertEquals(DisplayNameError.NoConnection, viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isSaving)
    }

    @Test
    fun editName_clearsError() {
        viewModel.onDisplayNameChange("Al")
        viewModel.confirm()

        viewModel.onDisplayNameChange("Alex")

        assertEquals(null, viewModel.uiState.value.error)
    }
}

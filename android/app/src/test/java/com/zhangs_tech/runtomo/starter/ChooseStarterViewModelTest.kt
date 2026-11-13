package com.zhangs_tech.runtomo.starter

import com.zhangs_tech.runtomo.cache.FakePlayerCache
import com.zhangs_tech.runtomo.MainDispatcherRule
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.auth.FakeAuthApi
import com.zhangs_tech.runtomo.auth.FakeTokenStore
import com.zhangs_tech.runtomo.auth.SessionState
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.tomo.FakeTomoApi
import com.zhangs_tech.runtomo.user.FakeUserApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ChooseStarterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tomoApi = FakeTomoApi()
    private val authRepository = AuthRepository(FakeAuthApi(), FakeTokenStore(), FakeUserApi(), FakePlayerCache())
    private val viewModel by lazy { ChooseStarterViewModel(tomoApi, authRepository) }
    private val luppy get() = tomoApi.starters[0]

    @Test
    fun init_loadsStarters() {
        assertEquals(tomoApi.starters, viewModel.uiState.value.starters)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun init_offline_showsNoConnectionThenRetryLoads() {
        tomoApi.getStartersError = ApiException.Network(IOException())
        assertEquals(StarterError.NoConnection, viewModel.uiState.value.error)

        tomoApi.getStartersError = null
        viewModel.loadStarters()

        assertEquals(tomoApi.starters, viewModel.uiState.value.starters)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun confirmSelection_usesSpeciesNameAsDefaultNickname() {
        viewModel.select(luppy)

        viewModel.confirmSelection()

        assertTrue(viewModel.uiState.value.isNaming)
        assertEquals("Luppy", viewModel.uiState.value.nickname)
    }

    @Test
    fun cancelNaming_returnsToStartersAndKeepsSelection() {
        viewModel.select(luppy)
        viewModel.confirmSelection()

        viewModel.cancelNaming()

        assertFalse(viewModel.uiState.value.isNaming)
        assertEquals(luppy, viewModel.uiState.value.selected)
    }

    @Test
    fun save_createsStarterAndIsReady() {
        viewModel.select(luppy)
        viewModel.confirmSelection()
        viewModel.onNicknameChange(" Mochi ")

        viewModel.save()

        assertEquals(listOf("luppy" to "Mochi"), tomoApi.chosenStarters)
        assertEquals(SessionState.Ready, authRepository.sessionState.value)
    }

    @Test
    fun save_emptyNickname_showsRule() {
        viewModel.select(luppy)
        viewModel.confirmSelection()
        viewModel.onNicknameChange(" ")

        viewModel.save()

        assertEquals(StarterError.InvalidNickname, viewModel.uiState.value.error)
        assertEquals(emptyList<Pair<String, String>>(), tomoApi.chosenStarters)
    }

    @Test
    fun save_offline_keepsChoiceForRetry() {
        viewModel.select(luppy)
        viewModel.confirmSelection()
        tomoApi.chooseStarterError = ApiException.Network(IOException())

        viewModel.save()

        val state = viewModel.uiState.value
        assertEquals(StarterError.NoConnection, state.error)
        assertTrue(state.isNaming)
        assertEquals(luppy, state.selected)
        assertEquals(SessionState.Loading, authRepository.sessionState.value)
    }

    @Test
    fun save_conflict_treatedAsSuccess() {
        viewModel.select(luppy)
        viewModel.confirmSelection()
        tomoApi.chooseStarterError = ApiException.Conflict(IllegalStateException())

        viewModel.save()

        assertEquals(SessionState.Ready, authRepository.sessionState.value)
    }
}

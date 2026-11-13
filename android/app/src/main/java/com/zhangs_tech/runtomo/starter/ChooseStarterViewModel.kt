package com.zhangs_tech.runtomo.starter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.tomo.Nickname
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.TomoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StarterError { NoConnection, Failed, InvalidNickname }

data class ChooseStarterUiState(
    val starters: List<Species> = emptyList(),
    val isLoading: Boolean = true,
    val selected: Species? = null,
    /** True after the Player confirms [selected] and gives it a nickname (UC-02 step 5). */
    val isNaming: Boolean = false,
    val nickname: String = "",
    val isSaving: Boolean = false,
    val error: StarterError? = null,
)

@HiltViewModel
class ChooseStarterViewModel @Inject constructor(
    private val tomoApi: TomoApi,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChooseStarterUiState())
    val uiState: StateFlow<ChooseStarterUiState> = _uiState.asStateFlow()

    init {
        loadStarters()
    }

    fun loadStarters() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val starters = tomoApi.getStarters()
                _uiState.update { it.copy(starters = starters, isLoading = false) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.toStarterError()) }
            }
        }
    }

    fun select(species: Species) {
        _uiState.update { it.copy(selected = species) }
    }

    fun confirmSelection() {
        val selected = _uiState.value.selected ?: return
        _uiState.update { it.copy(isNaming = true, nickname = selected.name, error = null) }
    }

    /** UC-02 4a: the Player goes back to the starters. */
    fun cancelNaming() {
        _uiState.update { it.copy(isNaming = false, error = null) }
    }

    fun onNicknameChange(nickname: String) {
        _uiState.update { it.copy(nickname = nickname, error = null) }
    }

    /** On a connection error the choice stays, so the Player can try again (UC-02 7a). */
    fun save() {
        val state = _uiState.value
        val species = state.selected ?: return
        if (state.isSaving) return
        val nickname = state.nickname.trim()
        if (!Nickname.isValid(nickname)) {
            _uiState.update { it.copy(error = StarterError.InvalidNickname) }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                tomoApi.chooseStarter(species.code, nickname)
                authRepository.onStarterTomoChosen()
            } catch (e: ApiException.Conflict) {
                // An earlier request worked, but its response was lost.
                authRepository.onStarterTomoChosen()
            } catch (e: ApiException) {
                _uiState.update { it.copy(isSaving = false, error = e.toStarterError()) }
            }
        }
    }

    private fun ApiException.toStarterError() =
        if (this is ApiException.Network) StarterError.NoConnection else StarterError.Failed
}

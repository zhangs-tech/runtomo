package com.zhangs_tech.runtomo.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.network.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DisplayNameError { Invalid, NoConnection, Failed }

data class ConfirmDisplayNameUiState(
    val displayName: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: DisplayNameError? = null,
)

@HiltViewModel
class ConfirmDisplayNameViewModel @Inject constructor(
    private val userApi: UserApi,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConfirmDisplayNameUiState())
    val uiState: StateFlow<ConfirmDisplayNameUiState> = _uiState.asStateFlow()

    init {
        loadDefaultName()
    }

    /** The default is the name from the Google profile, which the server saved at sign-up. */
    private fun loadDefaultName() {
        viewModelScope.launch {
            _uiState.value = try {
                ConfirmDisplayNameUiState(displayName = userApi.getMe().displayName, isLoading = false)
            } catch (e: ApiException) {
                ConfirmDisplayNameUiState(isLoading = false)
            }
        }
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.update { it.copy(displayName = displayName, error = null) }
    }

    fun confirm() {
        val state = _uiState.value
        if (state.isSaving) return
        val displayName = state.displayName.trim()
        if (!DisplayName.isValid(displayName)) {
            _uiState.update { it.copy(error = DisplayNameError.Invalid) }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val error = try {
                userApi.updateDisplayName(displayName)
                authRepository.onDisplayNameConfirmed()
                null
            } catch (e: ApiException.Network) {
                DisplayNameError.NoConnection
            } catch (e: ApiException) {
                DisplayNameError.Failed
            }
            _uiState.update { it.copy(isSaving = false, error = error) }
        }
    }
}

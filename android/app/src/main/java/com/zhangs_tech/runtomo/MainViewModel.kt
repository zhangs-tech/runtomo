package com.zhangs_tech.runtomo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.auth.AuthRepository
import com.zhangs_tech.runtomo.auth.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = authRepository.sessionState

    init {
        viewModelScope.launch { authRepository.restoreSession() }
    }
}

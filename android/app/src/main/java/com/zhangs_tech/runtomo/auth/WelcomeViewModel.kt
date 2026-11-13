package com.zhangs_tech.runtomo.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.network.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SignInError { NoConnection, NoGoogleAccount, SignInFailed }

data class WelcomeUiState(
    val isSigningIn: Boolean = false,
    val error: SignInError? = null,
)

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val identityProvider: IdentityProvider,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    fun signIn(activityContext: Context) {
        if (_uiState.value.isSigningIn) return
        _uiState.value = WelcomeUiState(isSigningIn = true)

        viewModelScope.launch {
            val error = when (val result = identityProvider.requestIdToken(activityContext)) {
                is IdTokenResult.Success -> signInToServer(result.idToken)
                IdTokenResult.Cancelled -> null
                IdTokenResult.NoAccount -> SignInError.NoGoogleAccount
                is IdTokenResult.Failed -> {
                    Log.w(TAG, "Google sign-in failed", result.cause)
                    SignInError.SignInFailed
                }
            }
            _uiState.value = WelcomeUiState(error = error)
        }
    }

    private suspend fun signInToServer(idToken: String): SignInError? =
        try {
            authRepository.signIn(idToken)
            null
        } catch (e: ApiException) {
            Log.w(TAG, "Server sign-in failed", e)
            if (e is ApiException.Network) SignInError.NoConnection else SignInError.SignInFailed
        }

    private companion object {
        const val TAG = "WelcomeViewModel"
    }
}

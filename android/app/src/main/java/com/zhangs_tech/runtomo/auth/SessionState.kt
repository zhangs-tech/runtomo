package com.zhangs_tech.runtomo.auth

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data object NeedsStarterTomo : SessionState
    data object Ready : SessionState
}

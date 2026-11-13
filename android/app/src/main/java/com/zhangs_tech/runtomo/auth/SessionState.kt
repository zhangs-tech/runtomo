package com.zhangs_tech.runtomo.auth

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState

    /** A new Player confirms or changes the display name from Google (UC-01 step 7). */
    data object NeedsDisplayName : SessionState
    data object NeedsStarterTomo : SessionState
    data object Ready : SessionState
}

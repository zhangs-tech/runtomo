package com.zhangs_tech.runtomo.auth

import android.content.Context

sealed interface IdTokenResult {
    data class Success(val idToken: String) : IdTokenResult
    data object Cancelled : IdTokenResult
    data object NoAccount : IdTokenResult
    data class Failed(val cause: Throwable) : IdTokenResult
}

interface IdentityProvider {
    /** Shows the account picker. [activityContext] must be an Activity context. */
    suspend fun requestIdToken(activityContext: Context): IdTokenResult
}

package com.zhangs_tech.runtomo.auth

data class AuthTokens(val accessToken: String, val refreshToken: String)

data class SignInResult(val tokens: AuthTokens, val isNewUser: Boolean)

/** The RunTomo server auth endpoints. Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface AuthApi {
    suspend fun signIn(idToken: String): SignInResult

    /** Returns a new token pair. The server revokes [refreshToken]. */
    suspend fun refresh(refreshToken: String): AuthTokens

    suspend fun signOut(refreshToken: String)
}

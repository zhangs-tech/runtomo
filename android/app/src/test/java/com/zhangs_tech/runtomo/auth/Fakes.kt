package com.zhangs_tech.runtomo.auth

import android.content.Context
import com.zhangs_tech.runtomo.network.ApiException

class FakeAuthApi : AuthApi {
    var signInResult: Result<SignInResult> =
        Result.success(SignInResult(AuthTokens("access", "refresh"), isNewUser = false))
    var refreshResult: Result<AuthTokens> = Result.success(AuthTokens("new-access", "new-refresh"))
    var signOutError: ApiException? = null
    val signedOutRefreshTokens = mutableListOf<String>()

    override suspend fun signIn(idToken: String): SignInResult = signInResult.getOrThrow()

    override suspend fun refresh(refreshToken: String): AuthTokens = refreshResult.getOrThrow()

    override suspend fun signOut(refreshToken: String) {
        signedOutRefreshTokens += refreshToken
        signOutError?.let { throw it }
    }
}

class FakeTokenStore(var tokens: AuthTokens? = null) : TokenStore {
    override suspend fun read(): AuthTokens? = tokens
    override suspend fun save(tokens: AuthTokens) {
        this.tokens = tokens
    }
    override suspend fun clear() {
        tokens = null
    }
}

class FakeIdentityProvider(var result: IdTokenResult = IdTokenResult.Success("id-token")) : IdentityProvider {
    override suspend fun requestIdToken(activityContext: Context): IdTokenResult = result
}

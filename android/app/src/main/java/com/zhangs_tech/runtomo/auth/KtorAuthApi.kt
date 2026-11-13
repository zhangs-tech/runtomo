package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

class KtorAuthApi @Inject constructor(private val httpClient: HttpClient) : AuthApi {

    override suspend fun signIn(idToken: String): SignInResult = apiCall {
        val response: SignInResponse = httpClient.post("auth/signin") {
            contentType(ContentType.Application.Json)
            setBody(SignInRequest(idToken))
        }.body()
        SignInResult(AuthTokens(response.accessToken, response.refreshToken), response.isNewUser)
    }

    override suspend fun refresh(refreshToken: String): AuthTokens = apiCall {
        val response: RefreshTokenResponse = httpClient.post("auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken))
        }.body()
        AuthTokens(response.accessToken, response.refreshToken)
    }

    override suspend fun signOut(refreshToken: String) {
        apiCall {
            httpClient.post("auth/signout") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }
        }
    }
}

@Serializable
private data class SignInRequest(val idToken: String)

@Serializable
private data class SignInResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)

@Serializable
private data class RefreshTokenRequest(val refreshToken: String)

@Serializable
private data class RefreshTokenResponse(val accessToken: String, val refreshToken: String)

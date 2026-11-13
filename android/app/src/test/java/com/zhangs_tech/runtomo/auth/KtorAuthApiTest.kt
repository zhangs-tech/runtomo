package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class KtorAuthApiTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun authApi(handler: MockRequestHandler) =
        KtorAuthApi(createHttpClient(MockEngine(handler), "http://server.test/"))

    @Test
    fun signIn_postsIdTokenAndReturnsTokens() = runTest {
        var requestBody = ""
        val api = authApi { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/auth/signin", request.url.encodedPath)
            requestBody = request.body.toByteArray().decodeToString()
            respond(
                """{"accessToken":"a","refreshToken":"r","accessTokenExpiresIn":900,"isNewUser":true}""",
                HttpStatusCode.OK,
                jsonHeaders,
            )
        }

        val result = api.signIn("google-id-token")

        assertEquals("""{"idToken":"google-id-token"}""", requestBody)
        assertEquals(SignInResult(AuthTokens("a", "r"), isNewUser = true), result)
    }

    @Test
    fun refresh_postsRefreshTokenAndReturnsNewTokens() = runTest {
        var requestBody = ""
        val api = authApi { request ->
            assertEquals("/auth/refresh", request.url.encodedPath)
            requestBody = request.body.toByteArray().decodeToString()
            respond("""{"accessToken":"a2","accessTokenExpiresIn":900,"refreshToken":"r2"}""", HttpStatusCode.OK, jsonHeaders)
        }

        val tokens = api.refresh("r1")

        assertEquals("""{"refreshToken":"r1"}""", requestBody)
        assertEquals(AuthTokens("a2", "r2"), tokens)
    }

    @Test
    fun signOut_postsRefreshToken() = runTest {
        var path = ""
        val api = authApi { request ->
            path = request.url.encodedPath
            respond("", HttpStatusCode.NoContent)
        }

        api.signOut("r1")

        assertEquals("/auth/signout", path)
    }

    @Test(expected = ApiException.Unauthorized::class)
    fun unauthorizedResponse_throwsUnauthorized() = runTest {
        authApi { respond("", HttpStatusCode.Unauthorized) }.signIn("bad-token")
    }

    @Test(expected = ApiException.Failed::class)
    fun serverError_throwsFailed() = runTest {
        authApi { respond("", HttpStatusCode.InternalServerError) }.signIn("token")
    }

    @Test(expected = ApiException.Failed::class)
    fun malformedBody_throwsFailed() = runTest {
        authApi { respond("""{"unexpected":true}""", HttpStatusCode.OK, jsonHeaders) }.signIn("token")
    }

    @Test(expected = ApiException.Network::class)
    fun connectionFailure_throwsNetwork() = runTest {
        authApi { throw IOException("offline") }.signIn("token")
    }
}

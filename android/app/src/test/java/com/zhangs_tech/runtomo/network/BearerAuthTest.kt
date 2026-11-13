package com.zhangs_tech.runtomo.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BearerAuthTest {

    private class FakeTokenProvider(var token: String?) : AccessTokenProvider {
        var refreshedFrom: String? = null
        override suspend fun accessToken(): String? = token
        override suspend fun refreshAccessToken(staleAccessToken: String?): String? {
            refreshedFrom = staleAccessToken
            token = "fresh"
            return token
        }
    }

    private val authHeaders = mutableListOf<String?>()

    private fun client(tokenProvider: AccessTokenProvider, acceptedToken: String) =
        createHttpClient(
            MockEngine { request ->
                val header = request.headers[HttpHeaders.Authorization]
                authHeaders += header
                if (header == "Bearer $acceptedToken") {
                    respond("ok")
                } else {
                    respond("", HttpStatusCode.Unauthorized, headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
                }
            },
            "http://server.test/",
        ).withBearerAuth { tokenProvider }

    @Test
    fun request_sendsStoredAccessToken() = runTest {
        client(FakeTokenProvider("stored"), acceptedToken = "stored").get("me")

        assertEquals(listOf("Bearer stored"), authHeaders)
    }

    @Test
    fun rejectedToken_refreshesOnceAndRetries() = runTest {
        val tokenProvider = FakeTokenProvider("expired")

        client(tokenProvider, acceptedToken = "fresh").get("me")

        assertEquals("expired", tokenProvider.refreshedFrom)
        assertEquals(listOf("Bearer expired", "Bearer fresh"), authHeaders)
    }

    @Test
    fun tokenChange_isUsedOnNextRequest() = runTest {
        val tokenProvider = FakeTokenProvider("first")
        val client = client(tokenProvider, acceptedToken = "second")
        tokenProvider.token = "second"

        client.get("me")

        assertEquals(listOf("Bearer second"), authHeaders)
    }
}

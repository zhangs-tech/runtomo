package com.zhangs_tech.runtomo.user

import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorUserApiTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun getMe_readsPlayerWithoutTomo() = runTest {
        val api = KtorUserApi(
            createHttpClient(
                MockEngine { request ->
                    assertEquals("/me", request.url.encodedPath)
                    respond(
                        """{"id":"u1","displayName":"Kai","friendCode":"00000001","coinBalance":120,"equippedTomoId":null}""",
                        headers = jsonHeaders,
                    )
                },
                "http://server.test/",
            ),
        )

        assertEquals(Me("Kai", "00000001", 120, hasTomo = false), api.getMe())
    }

    @Test
    fun updateDisplayName_patchesMe() = runTest {
        var requestBody = ""
        val api = KtorUserApi(
            createHttpClient(
                MockEngine { request ->
                    assertEquals(HttpMethod.Patch, request.method)
                    requestBody = request.body.toByteArray().decodeToString()
                    respond(
                        """{"id":"u1","displayName":"Kai","friendCode":"00000001","coinBalance":0,"equippedTomoId":"t1"}""",
                        headers = jsonHeaders,
                    )
                },
                "http://server.test/",
            ),
        )

        val me = api.updateDisplayName("Kai")

        assertEquals("""{"displayName":"Kai"}""", requestBody)
        assertEquals(true, me.hasTomo)
    }
}

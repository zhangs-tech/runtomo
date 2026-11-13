package com.zhangs_tech.runtomo.quest

import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorQuestApiTest {
    private fun questApi(handler: MockRequestHandler) =
        KtorQuestApi(createHttpClient(MockEngine(handler), "http://server.test/"))

    @Test
    fun claim_postsAndReadsBalanceAndQuests() = runTest {
        val api = questApi { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/quests/walk-1km/claim", request.url.encodedPath)
            respond(
                """{"coinBalance":220,"quests":[{"code":"walk-1km","title":"Walk","metric":"DISTANCE","target":1000,"progress":1200,"reward":100,"status":"CLAIMED"}]}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val result = api.claim("walk-1km")

        assertEquals(220, result.coinBalance)
        assertEquals(QuestStatus.CLAIMED, result.quests.single().status)
    }

    @Test(expected = ApiException.Conflict::class)
    fun claim_alreadyClaimed_throwsConflict() = runTest {
        questApi { respond("", HttpStatusCode.Conflict) }.claim("walk-1km")
    }
}

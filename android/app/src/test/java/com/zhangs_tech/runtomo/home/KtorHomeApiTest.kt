package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.network.createHttpClient
import com.zhangs_tech.runtomo.quest.QuestMetric
import com.zhangs_tech.runtomo.quest.QuestStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorHomeApiTest {
    @Test
    fun getHome_readsServerResponse() = runTest {
        val api = KtorHomeApi(
            createHttpClient(
                MockEngine { request ->
                    assertEquals("/home", request.url.encodedPath)
                    respond(
                        """
                        {"displayName":"Kai","mood":"BORED","dailyGoalM":5000,"strideM":0.75,"coinBalance":0,
                         "lastSyncDate":null,"today":{"date":"2026-11-14","steps":0,"distanceM":0},
                         "tomo":{"id":"t1","nickname":"Mochi","bondPoints":0,"bondLevel":1,"distanceTogetherM":0,
                           "birthday":"2026-11-14","species":{"code":"luppy","name":"Luppy","rarity":"COMMON","description":"Runs hot."}},
                         "quests":[{"code":"walk-1km","title":"Walk or run 1 km","metric":"DISTANCE","target":1000,
                           "progress":0,"reward":100,"status":"IN_PROGRESS"}]}
                        """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
                "http://server.test/",
            ),
        )

        val home = api.getHome()

        assertEquals("Mochi", home.tomo!!.nickname)
        assertEquals("Luppy", home.tomo.species.name)
        assertEquals(QuestMetric.DISTANCE, home.quests.single().metric)
        assertEquals(QuestStatus.IN_PROGRESS, home.quests.single().status)
        assertEquals(null, home.lastSyncDate)
    }
}

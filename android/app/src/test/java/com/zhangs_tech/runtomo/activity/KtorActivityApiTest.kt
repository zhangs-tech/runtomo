package com.zhangs_tech.runtomo.activity

import com.zhangs_tech.runtomo.network.createHttpClient
import com.zhangs_tech.runtomo.tomo.Mood
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class KtorActivityApiTest {
    @Test
    fun sync_putsStepsAndReadsRewards() = runTest {
        var requestBody = ""
        val api = KtorActivityApi(
            createHttpClient(
                MockEngine { request ->
                    assertEquals(HttpMethod.Put, request.method)
                    assertEquals("/me/days/2026-11-14", request.url.encodedPath)
                    requestBody = request.body.toByteArray().decodeToString()
                    respond(
                        """
                        {"date":"2026-11-14","steps":1600,"distanceM":1200,"strideM":0.75,"coinBalance":120,"mood":"HAPPY",
                         "rewards":{"distanceM":1200,"coins":120,"coinCapReached":false,"bond":12,"bondLevelBefore":1,
                         "bondLevelAfter":1,"distanceTogetherM":1200,"tomoNickname":"Mochi",
                         "completedQuests":[{"code":"walk-1km","title":"Walk or run 1 km","reward":100}],"showRewardScreen":true}}
                        """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
                "http://server.test/",
            ),
        )

        val sync = api.sync(LocalDate.of(2026, 11, 14), 1600)

        assertEquals("""{"steps":1600}""", requestBody)
        assertEquals(Mood.HAPPY, sync.mood)
        assertEquals("Mochi", sync.rewards!!.tomoNickname)
        assertEquals(CompletedQuest("walk-1km", "Walk or run 1 km", 100), sync.rewards.completedQuests.single())
    }
}

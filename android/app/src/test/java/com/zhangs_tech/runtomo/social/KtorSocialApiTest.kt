package com.zhangs_tech.runtomo.social

import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KtorSocialApiTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val problem = headersOf(HttpHeaders.ContentType, "application/problem+json")
    private val playerJson = """{"userId":"u2","displayName":"Ana","tomo":{"nickname":"Mochi","species":{"code":"luppy","name":"Luppy","rarity":"COMMON","description":"Hot."}}}"""

    private fun api(handler: MockRequestHandler) = KtorSocialApi(createHttpClient(MockEngine(handler), "http://server.test/"))

    private fun conflict(type: String): MockRequestHandler = { respond("""{"type":"$type"}""", HttpStatusCode.Conflict, problem) }

    @Test
    fun lookUp_mapsEveryOutcome() = runTest {
        assertEquals("Ana", (api { respond(playerJson, headers = json) }.lookUp("1") as LookUpResult.Found).player.displayName)
        assertEquals(LookUpResult.NotFound, api { respond("", HttpStatusCode.NotFound) }.lookUp("1"))
        assertEquals(LookUpResult.OwnCode, api(conflict("/problems/own-friend-code")).lookUp("1"))
        assertEquals(LookUpResult.AlreadyFriends, api(conflict("/problems/already-friends")).lookUp("1"))
    }

    @Test
    fun sendRequest_mapsEveryOutcome() = runTest {
        assertEquals(SendResult.SENT, api { respond("""{"status":"SENT"}""", HttpStatusCode.Created, json) }.sendRequest("u2"))
        assertEquals(SendResult.NOW_FRIENDS, api { respond("""{"status":"NOW_FRIENDS"}""", HttpStatusCode.Created, json) }.sendRequest("u2"))
        assertEquals(SendResult.LIMIT_REACHED, api(conflict("/problems/friend-limit")).sendRequest("u2"))
    }

    @Test
    fun accept_atLimit_returnsFalse() = runTest {
        assertFalse(api(conflict("/problems/friend-limit")).accept("r1"))
    }

    @Test
    fun leaderboard_sendsPeriodAndReadsGap() = runTest {
        val board = api { request ->
            assertEquals("month", request.url.parameters["period"])
            respond("""{"period":"month","rows":[{"rank":1,"player":$playerJson,"distanceM":3000,"isMe":false}],"ahead":$playerJson,"gapM":1800}""", headers = json)
        }.leaderboard(LeaderboardPeriod.MONTH)

        assertEquals(1800L, board.gapM)
        assertEquals("Ana", board.ahead!!.displayName)
    }
}

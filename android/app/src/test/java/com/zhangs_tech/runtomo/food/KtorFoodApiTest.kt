package com.zhangs_tech.runtomo.food

import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorFoodApiTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val problem = headersOf(HttpHeaders.ContentType, "application/problem+json")
    private val pantryJson = """{"items":[{"code":"banana","name":"Banana","cost":50,"bond":3,"quantity":1}],"coinBalance":150,"tomo":null,"feedsLeftToday":3}"""

    private fun api(handler: MockRequestHandler) = KtorFoodApi(createHttpClient(MockEngine(handler), "http://server.test/"))

    @Test
    fun getPantry_readsItems() = runTest {
        assertEquals(1, api { respond(pantryJson, headers = json) }.getPantry().items.single().quantity)
    }

    @Test
    fun buy_notEnoughCoins_readsShortfall() = runTest {
        val attempt = api {
            respond("""{"type":"/problems/not-enough-coins","shortfall":50,"distanceNeededM":500}""", HttpStatusCode.Conflict, problem)
        }.buy("bento")

        assertEquals(BuyAttempt.NotEnoughCoins(50, 500), attempt)
    }

    @Test
    fun feed_postsFoodAndReadsPantry() = runTest {
        var body = ""
        val attempt = api { request ->
            assertEquals("/me/tomos/t1/feed", request.url.encodedPath)
            body = request.body.toByteArray().decodeToString()
            respond("""{"bondLevelBefore":1,"pantry":$pantryJson}""", headers = json)
        }.feed("t1", "banana")

        assertEquals("""{"foodCode":"banana"}""", body)
        assertEquals(1, (attempt as FeedAttempt.Fed).bondLevelBefore)
    }

    @Test
    fun feed_full_isFull() = runTest {
        val attempt = api { respond("""{"type":"/problems/feeding-limit"}""", HttpStatusCode.Conflict, problem) }.feed("t1", "banana")

        assertEquals(FeedAttempt.Full, attempt)
    }

    @Test(expected = ApiException.Conflict::class)
    fun feed_otherConflict_throws() = runTest {
        api { respond("""{"type":"/problems/other"}""", HttpStatusCode.Conflict, problem) }.feed("t1", "banana")
    }
}

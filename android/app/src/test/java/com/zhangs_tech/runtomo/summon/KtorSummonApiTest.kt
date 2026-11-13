package com.zhangs_tech.runtomo.summon

import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KtorSummonApiTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun api(handler: MockRequestHandler) = KtorSummonApi(createHttpClient(MockEngine(handler), "http://server.test/"))

    private val resultJson = """
        {"id":"s1","bannerCode":"standard","species":{"code":"luppy","name":"Luppy","rarity":"COMMON","description":"Hot."},
         "wasPity":false,"wasDuplicate":true,"duplicateCoins":200,"tomo":null,"coinBalance":700,"createdAt":"2026-11-14T05:00:00Z"}
    """.trimIndent()

    @Test
    fun getBanner_readsRatesAndPity() = runTest {
        val banner = api { request ->
            assertEquals("/banners/standard", request.url.encodedPath)
            respond(
                """{"code":"standard","name":"Standard Egg","cost":1000,"coinBalance":1200,"pityRemaining":null,
                   "pool":[{"species":{"code":"luppy","name":"Luppy","rarity":"COMMON","description":"Hot."},"ratePercent":100.0}],
                   "rarityRates":[{"rarity":"COMMON","ratePercent":100.0}]}""",
                headers = json,
            )
        }.getBanner("standard")

        assertEquals(1000, banner.cost)
        assertNull(banner.pityRemaining)
        assertEquals(100.0, banner.pool.single().ratePercent, 0.0)
    }

    @Test
    fun summon_success_readsResult() = runTest {
        val attempt = api { respond(resultJson, HttpStatusCode.Created, json) }.summon("standard")

        val result = (attempt as SummonAttempt.Success).result
        assertEquals(200, result.duplicateCoins)
        assertNull(result.tomo)
    }

    @Test
    fun summon_notEnoughCoins_readsShortfall() = runTest {
        val attempt = api {
            respond(
                """{"type":"/problems/not-enough-coins","status":409,"shortfall":300,"distanceNeededM":3000}""",
                HttpStatusCode.Conflict,
                headersOf(HttpHeaders.ContentType, "application/problem+json"),
            )
        }.summon("standard")

        assertEquals(SummonAttempt.NotEnoughCoins(300, 3000), attempt)
    }

    @Test(expected = ApiException.Failed::class)
    fun summon_serverError_throws() = runTest {
        api { respond("", HttpStatusCode.InternalServerError) }.summon("standard")
    }

    @Test
    fun latest_readsFirstOrNull() = runTest {
        assertEquals("s1", api { request ->
            assertEquals("1", request.url.parameters["limit"])
            respond("[$resultJson]", headers = json)
        }.latest()!!.id)
        assertNull(api { respond("[]", headers = json) }.latest())
    }
}

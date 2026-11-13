package com.zhangs_tech.runtomo.history

import com.zhangs_tech.runtomo.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorHistoryApiTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun getTotalsAndDays_readResponses() = runTest {
        val api = KtorHistoryApi(
            createHttpClient(
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/me/totals" -> respond(
                            """{"week":{"steps":3000,"distanceM":2250},"month":{"steps":9000,"distanceM":6750}}""",
                            headers = json,
                        )
                        else -> {
                            assertEquals("2026-11-13", request.url.parameters["cursor"])
                            respond(
                                """{"days":[{"date":"2026-11-12","steps":2000,"distanceM":1500,"coins":150,"bond":15}],"nextCursor":null}""",
                                headers = json,
                            )
                        }
                    }
                },
                "http://server.test/",
            ),
        )

        assertEquals(6750L, api.getTotals().month.distanceM)
        assertEquals(HistoryPage(listOf(HistoryDay("2026-11-12", 2000, 1500, 150, 15))), api.getDays("2026-11-13"))
    }
}

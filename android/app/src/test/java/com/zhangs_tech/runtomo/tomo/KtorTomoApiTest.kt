package com.zhangs_tech.runtomo.tomo

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

class KtorTomoApiTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun tomoApi(handler: MockRequestHandler) =
        KtorTomoApi(createHttpClient(MockEngine(handler), "http://server.test/"))

    @Test
    fun getStarters_readsSpecies() = runTest {
        val api = tomoApi { request ->
            assertEquals("/species/starters", request.url.encodedPath)
            respond(
                """[{"code":"luppy","name":"Luppy","rarity":"COMMON","description":"Runs hot."}]""",
                headers = jsonHeaders,
            )
        }

        assertEquals(listOf(Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")), api.getStarters())
    }

    @Test
    fun chooseStarter_postsSpeciesAndNickname() = runTest {
        var requestBody = ""
        val api = tomoApi { request ->
            assertEquals("/me/tomos/starter", request.url.encodedPath)
            requestBody = request.body.toByteArray().decodeToString()
            respond("{}", HttpStatusCode.Created, jsonHeaders)
        }

        api.chooseStarter("luppy", "Mochi")

        assertEquals("""{"speciesCode":"luppy","nickname":"Mochi"}""", requestBody)
    }

    @Test(expected = ApiException.Conflict::class)
    fun chooseStarter_whenAlreadyChosen_throwsConflict() = runTest {
        tomoApi { respond("", HttpStatusCode.Conflict) }.chooseStarter("luppy", "Mochi")
    }
}

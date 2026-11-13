package com.zhangs_tech.runtomo.summon

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class KtorSummonApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : SummonApi {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getBanner(code: String): Banner = apiCall { httpClient.get("banners/$code").body() }

    override suspend fun summon(bannerCode: String): SummonAttempt = apiCall {
        val response = httpClient.post("summons") {
            expectSuccess = false
            contentType(ContentType.Application.Json)
            setBody(SummonRequest(bannerCode))
        }
        when {
            response.status.isSuccess() -> SummonAttempt.Success(response.body())
            response.status == HttpStatusCode.Conflict -> {
                val problem = json.decodeFromString<NotEnoughCoinsProblem>(response.bodyAsText())
                SummonAttempt.NotEnoughCoins(problem.shortfall, problem.distanceNeededM)
            }
            else -> throw ResponseException(response, response.bodyAsText())
        }
    }

    override suspend fun latest(): SummonResult? = apiCall {
        httpClient.get("summons") { parameter("limit", 1) }.body<List<SummonResult>>().firstOrNull()
    }
}

@Serializable
private data class SummonRequest(val bannerCode: String)

@Serializable
private data class NotEnoughCoinsProblem(val shortfall: Long, val distanceNeededM: Long)

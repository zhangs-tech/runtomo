package com.zhangs_tech.runtomo.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

fun createHttpClient(engine: HttpClientEngine, baseUrl: String): HttpClient =
    HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest { url(baseUrl) }
    }

/** Adds the access token to every request, and refreshes it once after a 401. */
fun HttpClient.withBearerAuth(tokenProvider: () -> AccessTokenProvider): HttpClient =
    config {
        install(Auth) {
            bearer {
                // The tokens change on sign-in, refresh and sign-out, so read them for each request.
                cacheTokens = false
                sendWithoutRequest { true }
                loadTokens { tokenProvider().accessToken()?.let { BearerTokens(it, null) } }
                refreshTokens {
                    tokenProvider().refreshAccessToken(oldTokens?.accessToken)?.let { BearerTokens(it, null) }
                }
            }
        }
    }

/** An RFC 9457 problem body. Only the fields the app reads. */
@Serializable
data class Problem(val type: String? = null, val shortfall: Long? = null, val distanceNeededM: Long? = null)

private val problemJson = Json { ignoreUnknownKeys = true }

/** Reads the problem body, or null if the body is not a problem. */
suspend fun HttpResponse.problem(): Problem? =
    try {
        problemJson.decodeFromString<Problem>(bodyAsText())
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

/** Runs a Ktor call and converts its failures to [ApiException], so callers do not see Ktor types. */
suspend fun <T> apiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> throw ApiException.Unauthorized()
            HttpStatusCode.NotFound -> throw ApiException.NotFound(e)
            HttpStatusCode.Conflict -> throw ApiException.Conflict(e, e.response.problem())
            else -> throw ApiException.Failed(e)
        }
    } catch (e: IOException) {
        throw ApiException.Network(e)
    } catch (e: Exception) {
        throw ApiException.Failed(e)
    }

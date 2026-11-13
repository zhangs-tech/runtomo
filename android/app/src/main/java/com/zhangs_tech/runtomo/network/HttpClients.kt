package com.zhangs_tech.runtomo.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
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

/** Runs a Ktor call and converts its failures to [ApiException], so callers do not see Ktor types. */
suspend fun <T> apiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ResponseException) {
        if (e.response.status == HttpStatusCode.Unauthorized) throw ApiException.Unauthorized()
        throw ApiException.Failed(e)
    } catch (e: IOException) {
        throw ApiException.Network(e)
    } catch (e: Exception) {
        throw ApiException.Failed(e)
    }

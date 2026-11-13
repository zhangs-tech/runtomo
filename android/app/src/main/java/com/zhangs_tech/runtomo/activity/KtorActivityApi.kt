package com.zhangs_tech.runtomo.activity

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import java.time.LocalDate
import javax.inject.Inject

class KtorActivityApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : ActivityApi {
    override suspend fun sync(date: LocalDate, steps: Int): DaySync = apiCall {
        httpClient.put("me/days/$date") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(steps))
        }.body()
    }
}

@Serializable
private data class SyncRequest(val steps: Int)

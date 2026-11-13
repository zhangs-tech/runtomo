package com.zhangs_tech.runtomo.history

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class KtorHistoryApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : HistoryApi {
    override suspend fun getTotals(): PeriodTotals = apiCall { httpClient.get("me/totals").body() }

    override suspend fun getDays(cursor: String?): HistoryPage = apiCall {
        httpClient.get("me/days") { cursor?.let { parameter("cursor", it) } }.body()
    }
}

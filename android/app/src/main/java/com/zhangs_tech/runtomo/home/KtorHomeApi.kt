package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class KtorHomeApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : HomeApi {
    override suspend fun getHome(): HomeData = apiCall { httpClient.get("home").body() }
}

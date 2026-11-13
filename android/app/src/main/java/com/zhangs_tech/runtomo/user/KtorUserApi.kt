package com.zhangs_tech.runtomo.user

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

class KtorUserApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : UserApi {
    override suspend fun getMe(): Me = apiCall {
        httpClient.get("me").body<MeResponse>().toMe()
    }

    override suspend fun updateDisplayName(displayName: String): Me = apiCall {
        httpClient.patch("me") {
            contentType(ContentType.Application.Json)
            setBody(UpdateMeRequest(displayName))
        }.body<MeResponse>().toMe()
    }
}

@Serializable
private data class MeResponse(
    val displayName: String,
    val friendCode: String,
    val coinBalance: Long,
    val equippedTomoId: String?,
) {
    fun toMe() = Me(displayName, friendCode, coinBalance, hasTomo = equippedTomoId != null)
}

@Serializable
private data class UpdateMeRequest(val displayName: String)

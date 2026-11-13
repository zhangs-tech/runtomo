package com.zhangs_tech.runtomo.tomo

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

class KtorTomoApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : TomoApi {
    override suspend fun getStarters(): List<Species> = apiCall {
        httpClient.get("species/starters").body<List<Species>>()
    }

    override suspend fun chooseStarter(speciesCode: String, nickname: String) {
        apiCall {
            httpClient.post("me/tomos/starter") {
                contentType(ContentType.Application.Json)
                setBody(ChooseStarterRequest(speciesCode, nickname))
            }
        }
    }

    override suspend fun getCollection(): TomoCollection = apiCall { httpClient.get("me/tomos").body() }

    override suspend fun rename(tomoId: String, nickname: String): Tomo = apiCall {
        httpClient.patch("me/tomos/$tomoId") {
            contentType(ContentType.Application.Json)
            setBody(RenameRequest(nickname))
        }.body()
    }

    override suspend fun equip(tomoId: String) {
        apiCall {
            httpClient.put("me/equipped-tomo") {
                contentType(ContentType.Application.Json)
                setBody(EquipRequest(tomoId))
            }
        }
    }
}

@Serializable
private data class ChooseStarterRequest(val speciesCode: String, val nickname: String)

@Serializable
private data class RenameRequest(val nickname: String)

@Serializable
private data class EquipRequest(val tomoId: String)

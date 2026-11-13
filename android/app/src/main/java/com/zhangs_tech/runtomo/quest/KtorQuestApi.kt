package com.zhangs_tech.runtomo.quest

import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import kotlinx.serialization.Serializable
import javax.inject.Inject

class KtorQuestApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : QuestApi {
    override suspend fun claim(questCode: String): ClaimResult = apiCall {
        val response = httpClient.post("quests/$questCode/claim").body<ClaimQuestResponse>()
        ClaimResult(response.coinBalance, response.quests)
    }
}

@Serializable
private data class ClaimQuestResponse(val coinBalance: Long, val quests: List<Quest>)

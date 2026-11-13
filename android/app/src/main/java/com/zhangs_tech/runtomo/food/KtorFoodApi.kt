package com.zhangs_tech.runtomo.food

import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

class KtorFoodApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : FoodApi {
    override suspend fun getPantry(): Pantry = apiCall { httpClient.get("food").body() }

    override suspend fun buy(foodCode: String): BuyAttempt =
        try {
            BuyAttempt.Bought(apiCall { httpClient.post("food/$foodCode/purchase").body<Pantry>() })
        } catch (e: ApiException.Conflict) {
            if (e.problemType != NOT_ENOUGH_COINS) throw e
            val problem = e.problem
            BuyAttempt.NotEnoughCoins(problem?.shortfall ?: 0, problem?.distanceNeededM ?: 0)
        }

    override suspend fun feed(tomoId: String, foodCode: String): FeedAttempt =
        try {
            val response = apiCall {
                httpClient.post("me/tomos/$tomoId/feed") {
                    contentType(ContentType.Application.Json)
                    setBody(FeedRequest(foodCode))
                }.body<FeedResponse>()
            }
            FeedAttempt.Fed(response.bondLevelBefore, response.pantry)
        } catch (e: ApiException.Conflict) {
            if (e.problemType != FEEDING_LIMIT) throw e
            FeedAttempt.Full
        }

    private companion object {
        const val NOT_ENOUGH_COINS = "/problems/not-enough-coins"
        const val FEEDING_LIMIT = "/problems/feeding-limit"
    }
}

@Serializable
private data class FeedRequest(val foodCode: String)

@Serializable
private data class FeedResponse(val bondLevelBefore: Int, val pantry: Pantry)

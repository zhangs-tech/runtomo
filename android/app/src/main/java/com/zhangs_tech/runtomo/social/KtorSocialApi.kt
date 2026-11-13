package com.zhangs_tech.runtomo.social

import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.network.AuthenticatedClient
import com.zhangs_tech.runtomo.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.Serializable
import javax.inject.Inject

class KtorSocialApi @Inject constructor(
    @AuthenticatedClient private val httpClient: HttpClient,
) : SocialApi {
    override suspend fun lookUp(friendCode: String): LookUpResult =
        try {
            LookUpResult.Found(apiCall { httpClient.get("users/by-code/${friendCode.encodeURLPathPart()}").body() })
        } catch (e: ApiException.NotFound) {
            LookUpResult.NotFound
        } catch (e: ApiException.Conflict) {
            when (e.problemType) {
                OWN_CODE -> LookUpResult.OwnCode
                ALREADY_FRIENDS -> LookUpResult.AlreadyFriends
                else -> throw e
            }
        }

    override suspend fun sendRequest(toUserId: String): SendResult =
        try {
            val response = apiCall {
                httpClient.post("friend-requests") {
                    contentType(ContentType.Application.Json)
                    setBody(SendRequestBody(toUserId))
                }.body<SendRequestResponse>()
            }
            if (response.status == "NOW_FRIENDS") SendResult.NOW_FRIENDS else SendResult.SENT
        } catch (e: ApiException.Conflict) {
            when (e.problemType) {
                ALREADY_FRIENDS -> SendResult.ALREADY_FRIENDS
                FRIEND_LIMIT -> SendResult.LIMIT_REACHED
                else -> throw e
            }
        }

    override suspend fun incomingRequests(): List<IncomingRequest> = apiCall { httpClient.get("friend-requests").body() }

    override suspend fun accept(requestId: String): Boolean =
        try {
            apiCall { httpClient.post("friend-requests/$requestId/accept") }
            true
        } catch (e: ApiException.Conflict) {
            if (e.problemType != FRIEND_LIMIT) throw e
            false
        }

    override suspend fun decline(requestId: String) {
        apiCall { httpClient.post("friend-requests/$requestId/decline") }
    }

    override suspend fun friends(): List<Friend> = apiCall { httpClient.get("friends").body() }

    override suspend fun remove(friendUserId: String) {
        apiCall { httpClient.delete("friends/$friendUserId") }
    }

    override suspend fun leaderboard(period: LeaderboardPeriod): Leaderboard = apiCall {
        httpClient.get("leaderboard") { parameter("period", period.param) }.body()
    }

    private companion object {
        const val OWN_CODE = "/problems/own-friend-code"
        const val ALREADY_FRIENDS = "/problems/already-friends"
        const val FRIEND_LIMIT = "/problems/friend-limit"
    }
}

@Serializable
private data class SendRequestBody(val toUserId: String)

@Serializable
private data class SendRequestResponse(val status: String)

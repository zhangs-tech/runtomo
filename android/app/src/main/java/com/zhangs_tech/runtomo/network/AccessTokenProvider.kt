package com.zhangs_tech.runtomo.network

interface AccessTokenProvider {
    suspend fun accessToken(): String?

    /**
     * Returns a new access token after the server rejected [staleAccessToken], or null if the
     * session ended or the server cannot be reached.
     */
    suspend fun refreshAccessToken(staleAccessToken: String?): String?
}

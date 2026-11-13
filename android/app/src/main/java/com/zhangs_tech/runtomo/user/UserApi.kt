package com.zhangs_tech.runtomo.user

data class Me(
    val displayName: String,
    val friendCode: String,
    val coinBalance: Long,
    val hasTomo: Boolean,
)

/** The RunTomo server `/me` endpoints. Every function throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface UserApi {
    suspend fun getMe(): Me
    suspend fun updateDisplayName(displayName: String): Me
}

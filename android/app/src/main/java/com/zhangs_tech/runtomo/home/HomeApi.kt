package com.zhangs_tech.runtomo.home

/** The RunTomo server `GET /home` endpoint. Throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface HomeApi {
    suspend fun getHome(): HomeData
}

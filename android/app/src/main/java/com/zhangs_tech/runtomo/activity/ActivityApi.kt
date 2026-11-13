package com.zhangs_tech.runtomo.activity

import java.time.LocalDate

/** The RunTomo server sync endpoint. Throws [com.zhangs_tech.runtomo.network.ApiException] on failure. */
interface ActivityApi {
    suspend fun sync(date: LocalDate, steps: Int): DaySync
}

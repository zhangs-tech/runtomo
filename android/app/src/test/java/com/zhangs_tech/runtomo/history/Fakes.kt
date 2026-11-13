package com.zhangs_tech.runtomo.history

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakeHistoryApi : HistoryApi {
    var error: Exception? = null
    var totals = PeriodTotals(ActivityTotal(3000, 2250), ActivityTotal(9000, 6750))
    val pages = mutableMapOf<String?, HistoryPage>(
        null to HistoryPage(
            listOf(HistoryDay("2026-11-14", 1600, 1200, 120, 12), HistoryDay("2026-11-13", 1400, 1050, 105, 10)),
            nextCursor = "2026-11-13",
        ),
        "2026-11-13" to HistoryPage(listOf(HistoryDay("2026-11-12", 2000, 1500, 150, 15))),
    )

    override suspend fun getTotals(): PeriodTotals {
        error?.let { throw it }
        return totals
    }

    override suspend fun getDays(cursor: String?): HistoryPage {
        error?.let { throw it }
        return pages.getValue(cursor)
    }
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [HistoryModule::class])
object FakeHistoryModule {
    @Provides
    @Singleton
    fun provideHistoryApi(): HistoryApi = FakeHistoryApi()
}

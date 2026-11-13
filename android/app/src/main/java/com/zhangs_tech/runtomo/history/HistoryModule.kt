package com.zhangs_tech.runtomo.history

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryModule {
    @Binds
    abstract fun bindHistoryApi(ktorHistoryApi: KtorHistoryApi): HistoryApi
}

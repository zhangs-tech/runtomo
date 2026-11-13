package com.zhangs_tech.runtomo.summon

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SummonModule {
    @Binds
    abstract fun bindSummonApi(ktorSummonApi: KtorSummonApi): SummonApi
}

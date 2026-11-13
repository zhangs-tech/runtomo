package com.zhangs_tech.runtomo.tomo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TomoModule {
    @Binds
    abstract fun bindTomoApi(ktorTomoApi: KtorTomoApi): TomoApi
}

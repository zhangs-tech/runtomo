package com.zhangs_tech.runtomo.core

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    fun provideClock(): Clock = Clock.systemUTC()
}

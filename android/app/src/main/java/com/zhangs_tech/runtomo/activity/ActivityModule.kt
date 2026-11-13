package com.zhangs_tech.runtomo.activity

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityModule {
    @Binds
    abstract fun bindActivityApi(ktorActivityApi: KtorActivityApi): ActivityApi

    @Binds
    abstract fun bindStepsReader(healthConnectStepsReader: HealthConnectStepsReader): StepsReader

    @Binds
    abstract fun bindBackgroundSync(workManagerBackgroundSync: WorkManagerBackgroundSync): BackgroundSync
}

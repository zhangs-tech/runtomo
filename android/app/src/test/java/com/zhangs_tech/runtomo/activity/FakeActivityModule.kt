package com.zhangs_tech.runtomo.activity

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ActivityModule::class])
object FakeActivityModule {
    @Provides
    @Singleton
    fun provideActivityApi(): ActivityApi = FakeActivityApi()

    @Provides
    @Singleton
    fun provideStepsReader(): StepsReader = FakeStepsReader()

    @Provides
    @Singleton
    fun provideBackgroundSync(): BackgroundSync = FakeBackgroundSync()
}

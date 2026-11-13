package com.zhangs_tech.runtomo.tomo

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [TomoModule::class])
object FakeTomoModule {
    @Provides
    @Singleton
    fun provideTomoApi(): TomoApi = FakeTomoApi()
}

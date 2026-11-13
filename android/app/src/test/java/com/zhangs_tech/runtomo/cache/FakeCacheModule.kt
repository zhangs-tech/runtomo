package com.zhangs_tech.runtomo.cache

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CacheModule::class])
object FakeCacheModule {
    @Provides
    @Singleton
    fun providePlayerCache(): PlayerCache = FakePlayerCache()
}

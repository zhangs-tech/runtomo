package com.zhangs_tech.runtomo.user

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserModule::class])
object FakeUserModule {
    @Provides
    @Singleton
    fun provideUserApi(): UserApi = FakeUserApi()
}

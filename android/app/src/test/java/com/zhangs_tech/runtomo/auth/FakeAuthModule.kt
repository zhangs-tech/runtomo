package com.zhangs_tech.runtomo.auth

import com.zhangs_tech.runtomo.network.AccessTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AuthModule::class])
object FakeAuthModule {
    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi = FakeAuthApi()

    @Provides
    @Singleton
    fun provideTokenStore(): TokenStore = FakeTokenStore()

    @Provides
    fun provideAccessTokenProvider(authRepository: AuthRepository): AccessTokenProvider = authRepository

    @Provides
    fun provideIdentityProvider(): IdentityProvider = FakeIdentityProvider()
}

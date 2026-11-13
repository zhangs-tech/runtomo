package com.zhangs_tech.runtomo.network

import com.zhangs_tech.runtomo.BuildConfig
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = createHttpClient(OkHttp.create(), BuildConfig.API_BASE_URL)

    // Lazy breaks the cycle: the token provider (AuthRepository) also uses APIs built on this client.
    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedHttpClient(
        httpClient: HttpClient,
        tokenProvider: Lazy<AccessTokenProvider>,
    ): HttpClient = httpClient.withBearerAuth { tokenProvider.get() }
}

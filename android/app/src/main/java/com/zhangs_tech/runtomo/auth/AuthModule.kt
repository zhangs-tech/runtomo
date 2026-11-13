package com.zhangs_tech.runtomo.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zhangs_tech.runtomo.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindAuthApi(ktorAuthApi: KtorAuthApi): AuthApi

    @Binds
    abstract fun bindTokenStore(dataStoreTokenStore: DataStoreTokenStore): TokenStore

    companion object {
        @Provides
        @Singleton
        fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(DataStoreTokenStore.FILE_NAME)
            }

        @Provides
        fun provideIdentityProvider(): IdentityProvider =
            GoogleIdentityProvider(BuildConfig.GOOGLE_WEB_CLIENT_ID)
    }
}

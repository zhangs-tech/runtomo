package com.zhangs_tech.runtomo.social

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SocialModule {
    @Binds
    abstract fun bindSocialApi(ktorSocialApi: KtorSocialApi): SocialApi
}

package com.zhangs_tech.runtomo.quest

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class QuestModule {
    @Binds
    abstract fun bindQuestApi(ktorQuestApi: KtorQuestApi): QuestApi
}

package com.zhangs_tech.runtomo.home

import com.zhangs_tech.runtomo.quest.QuestApi
import com.zhangs_tech.runtomo.quest.QuestModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [HomeModule::class, QuestModule::class])
object FakeHomeModule {
    @Provides
    @Singleton
    fun provideHomeApi(): HomeApi = FakeHomeApi()

    @Provides
    @Singleton
    fun provideQuestApi(): QuestApi = FakeQuestApi()
}

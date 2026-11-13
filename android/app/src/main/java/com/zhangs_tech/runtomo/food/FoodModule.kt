package com.zhangs_tech.runtomo.food

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FoodModule {
    @Binds
    abstract fun bindFoodApi(ktorFoodApi: KtorFoodApi): FoodApi
}

package com.app.datastore.di

import com.app.datastore.data.repository.ImageRepositoryImpl
import com.app.datastore.data.repository.PredictionHistoryRepositoryImpl
import com.app.datastore.domain.repository.ImageRepository
import com.app.datastore.domain.repository.PredictionHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Provides
    @Singleton
    fun provideImageRepository(): ImageRepository
    = ImageRepositoryImpl()

    @Provides
    @Singleton
    fun providePredictionHistoryRepository(): PredictionHistoryRepository
    = PredictionHistoryRepositoryImpl()
}
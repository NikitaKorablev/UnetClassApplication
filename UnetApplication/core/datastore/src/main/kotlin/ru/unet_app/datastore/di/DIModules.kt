package ru.unet_app.datastore.di

import android.content.Context
import ru.unet_app.datastore.data.repository.ImageRepositoryImpl
import ru.unet_app.datastore.data.repository.PredictionHistoryRepositoryImpl
import ru.unet_app.datastore.domain.repository.ImageRepository
import ru.unet_app.datastore.domain.repository.PredictionHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Provides
    @Singleton
    fun provideImageRepository(@ApplicationContext context: Context): ImageRepository
    = ImageRepositoryImpl(context)

    @Provides
    @Singleton
    fun providePredictionHistoryRepository(): PredictionHistoryRepository
    = PredictionHistoryRepositoryImpl()
}
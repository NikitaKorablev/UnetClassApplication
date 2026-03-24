package com.app.unet.di

import com.app.unet.domain.usecases.SplitImageIntoTilesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DIUseCases {
    @Provides
    @Singleton
    fun provideSplitImageIntoTilesUseCase() =
        SplitImageIntoTilesUseCase()
}
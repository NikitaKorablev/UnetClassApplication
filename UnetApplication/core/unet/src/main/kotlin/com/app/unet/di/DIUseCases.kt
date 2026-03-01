package com.app.unet.di

import com.app.datastore.domain.repository.ImageRepository
import com.app.unet.domain.usecases.SaveImageStitcherUseCase
import com.app.unet.domain.usecases.SplitImageIntoTilesUseCase
import com.app.unet.domain.usecases.StitchingImageUseCase
import com.app.unet.domain.usecases.TilesToTensorsUseCase
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
    fun provideSaveImageStitcherUseCase(
        imageRepository: ImageRepository
    ) = SaveImageStitcherUseCase(
        imageRepository
    )

    @Provides
    @Singleton
    fun provideSplitImageIntoTilesUseCase() =
        SplitImageIntoTilesUseCase()

    @Provides
    @Singleton
    fun provideStitchMaskUseCase() =
        StitchingImageUseCase()

    @Provides
    @Singleton
    fun provideTilesToTensorsUseCase() =
        TilesToTensorsUseCase()
}
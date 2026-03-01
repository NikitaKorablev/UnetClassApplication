package com.app.unet.di

import android.content.Context
import com.app.unet.domain.UnetModel
import com.app.unet.domain.usecases.SaveImageStitcherUseCase
import com.app.unet.domain.usecases.SplitImageIntoTilesUseCase
import com.app.unet.domain.usecases.StitchingImageUseCase
import com.app.unet.domain.usecases.TilesToTensorsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class Modules {
    @Provides
    @Singleton
    fun provideUnetModel(
        @ApplicationContext
        context: Context,
        stitchingImageUseCase: StitchingImageUseCase,
        splitImageIntoTilesUseCase: SplitImageIntoTilesUseCase,
        tilesToTensorsUseCase: TilesToTensorsUseCase,
        saveImageStitcherUseCase: SaveImageStitcherUseCase
    ) = UnetModel(
        context,
        stitchingImageUseCase,
        splitImageIntoTilesUseCase,
        tilesToTensorsUseCase,
        saveImageStitcherUseCase
    )
}
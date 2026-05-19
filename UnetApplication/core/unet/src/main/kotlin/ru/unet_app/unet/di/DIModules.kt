package ru.unet_app.unet.di

import android.content.Context
import ru.unet_app.unet.data.unetmodels.PyTorchModel
import ru.unet_app.unet.data.unetmodels.TFLiteModel
import ru.unet_app.unet.domain.UnetModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class Modules {
    @Provides
    @PytorchModel
    fun providePyTorchModel(
        @ApplicationContext
        context: Context,
    ): UnetModel = PyTorchModel(context)

    @Provides
    @LiteRTModel
    fun provideLiteRTModel(
        @ApplicationContext
        context: Context,
    ): UnetModel = TFLiteModel(context)
}
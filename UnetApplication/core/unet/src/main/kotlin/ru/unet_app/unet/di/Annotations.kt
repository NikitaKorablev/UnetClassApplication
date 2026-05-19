package ru.unet_app.unet.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PytorchModel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LiteRTModel
package com.app.transparency_settings.di

import com.app.transparency_settings.data.repository.TransparencyImageProcImpl
import com.app.transparency_settings.domain.repository.TransparencyImageProcRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DIModules {
    @Singleton
    @Provides
    fun provideTransparencyImageProcRepository(): TransparencyImageProcRepository
    = TransparencyImageProcImpl()
}
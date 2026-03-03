package com.app.transparancy.di

import com.app.transparancy.data.repository.TransparencyImageProcImpl
import com.app.transparancy.domain.repository.TransparencyImageProcRepository
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
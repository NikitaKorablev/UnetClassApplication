package com.app.unetclass.di

import com.app.domain.utils.Router
import com.app.unetclass.utils.AppNavigationComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilsModules {
    @Singleton
    @Provides
    fun provideRouter(): Router {
        return AppNavigationComponent()
    }
}
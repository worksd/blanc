package com.worksd.blanc.di

import android.content.Context
import com.worksd.blanc.util.PrefUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class UtilModule {

    @Singleton
    @Provides
    fun providePrefUtil(@ApplicationContext context: Context) = PrefUtils(context)

}
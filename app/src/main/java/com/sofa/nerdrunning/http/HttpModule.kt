package com.sofa.nerdrunning.http

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
class HttpModule {

    @Provides
    fun provideOkHttpClient() = OkHttpClient()

}

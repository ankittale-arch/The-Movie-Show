package com.ankitt.themovieshow.core.common.di

import com.ankitt.themovieshow.core.common.network.ConnectivityObserver
import com.ankitt.themovieshow.core.common.network.ConnectivityObserverImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {

    @Binds
    @Singleton
    abstract fun bindsConnectivityObserver(
        impl: ConnectivityObserverImpl,
    ): ConnectivityObserver
}

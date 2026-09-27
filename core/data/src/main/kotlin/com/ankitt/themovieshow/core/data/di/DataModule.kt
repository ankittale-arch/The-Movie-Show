package com.ankitt.themovieshow.core.data.di

import com.ankitt.themovieshow.core.data.*
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindsMovieRepository(impl: MovieRepositoryImpl): MovieRepository
}

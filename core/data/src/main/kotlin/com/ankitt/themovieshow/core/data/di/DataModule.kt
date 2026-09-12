package com.ankitt.themovieshow.core.data.di

import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.MovieRepositoryImpl
import dagger.Binds
import dagger.Module
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

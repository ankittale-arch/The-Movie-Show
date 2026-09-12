package com.ankitt.themovieshow.di

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Deliberately does not reuse `core:network`'s authenticated OkHttpClient (its AuthInterceptor
 * attaches the TMDB Bearer token to every request that client makes) — image.tmdb.org is a public
 * CDN that doesn't need or want that header, so Coil gets its own default, unauthenticated client.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    fun providesImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
}

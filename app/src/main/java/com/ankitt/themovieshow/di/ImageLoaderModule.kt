package com.ankitt.themovieshow.di

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
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
                // AnimatedImageDecoder uses the platform ImageDecoder (API 28+, more efficient);
                // GifDecoder is the pure-Kotlin fallback down to this app's minSdk 24.
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
}

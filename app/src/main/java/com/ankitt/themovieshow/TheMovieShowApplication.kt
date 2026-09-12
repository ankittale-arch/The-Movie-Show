package com.ankitt.themovieshow

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Root of the Hilt dependency graph (SingletonComponent). No app-level logic lives here on
 * purpose — WorkManager's periodic sync will be scheduled from here in Phase 6 via
 * androidx.startup rather than in onCreate directly, to keep this class from growing into the
 * God-class every app-level Application ends up as.
 *
 * Implements [SingletonImageLoader.Factory] to hand Coil the Hilt-provided [ImageLoader] (see
 * `di/ImageLoaderModule.kt`) instead of Coil building its own default one.
 */
@HiltAndroidApp
class TheMovieShowApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader
}

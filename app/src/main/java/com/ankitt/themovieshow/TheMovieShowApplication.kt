package com.ankitt.themovieshow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Root of the Hilt dependency graph (SingletonComponent). No app-level logic lives here on
 * purpose — the outbox's periodic sync is scheduled by `core:sync`'s `SyncInitializer` via
 * androidx.startup rather than in onCreate directly, to keep this class from growing into the
 * God-class every app-level Application ends up as. [workManagerConfiguration] is the one
 * exception: WorkManager's on-demand initialization requires it on the `Application` itself, and
 * it's what lets `SyncWorker` (an `@HiltWorker`) receive its dependencies from Hilt instead of a
 * no-arg constructor.
 *
 * Implements [SingletonImageLoader.Factory] to hand Coil the Hilt-provided [ImageLoader] (see
 * `di/ImageLoaderModule.kt`) instead of Coil building its own default one.
 */
@HiltAndroidApp
class TheMovieShowApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}

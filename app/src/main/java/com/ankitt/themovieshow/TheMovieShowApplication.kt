package com.ankitt.themovieshow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
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
 * [workManagerConfiguration] reads [HiltWorkerFactory] via [WorkerFactoryEntryPoint] rather than
 * field injection: `SyncInitializer` (an androidx.startup `Initializer`, see core:sync) triggers
 * WorkManager's on-demand init from inside `InitializationProvider.onCreate()`, which the OS runs
 * *before* `Application.onCreate()` — i.e. before Hilt has injected this class's fields. Reading
 * straight from the entry point instead works because the underlying Dagger component is already
 * built in `attachBaseContext`.
 *
 * Implements [SingletonImageLoader.Factory] to hand Coil the Hilt-provided [ImageLoader] (see
 * `di/ImageLoaderModule.kt`) instead of Coil building its own default one.
 */
@HiltAndroidApp
class TheMovieShowApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader

    override val workManagerConfiguration: Configuration
        get() {
            val workerFactory = EntryPointAccessors.fromApplication(this, WorkerFactoryEntryPoint::class.java)
                .workerFactory()
            return Configuration.Builder().setWorkerFactory(workerFactory).build()
        }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerFactoryEntryPoint {
    fun workerFactory(): HiltWorkerFactory
}

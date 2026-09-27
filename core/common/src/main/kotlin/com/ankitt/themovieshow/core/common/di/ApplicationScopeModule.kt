package com.ankitt.themovieshow.core.common.di

import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import javax.inject.*

/**
 * A process-lifetime [CoroutineScope], used only by singletons that must keep working after the
 * screen that triggered them (e.g. [com.ankitt.themovieshow.core.common.network.ConnectivityObserver]'s
 * shared StateFlow, and later the sync engine's in-flight coroutines) goes away. This is the one
 * sanctioned alternative to `GlobalScope`: it is still owned by the Hilt SingletonComponent, so it
 * is cancelled when the process dies, and it is injectable, so tests can substitute a
 * [kotlinx.coroutines.test.TestScope].
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object ApplicationScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationScope(
        @DefaultDispatcher defaultDispatcher: kotlinx.coroutines.CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
}

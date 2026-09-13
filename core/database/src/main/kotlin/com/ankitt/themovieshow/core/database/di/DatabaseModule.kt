package com.ankitt.themovieshow.core.database.di

import android.content.Context
import androidx.room.Room
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import com.ankitt.themovieshow.core.database.bookmark.BookmarkDao
import com.ankitt.themovieshow.core.database.migration.MIGRATION_1_2
import com.ankitt.themovieshow.core.database.migration.MIGRATION_2_3
import com.ankitt.themovieshow.core.database.migration.MIGRATION_3_4
import com.ankitt.themovieshow.core.database.migration.MIGRATION_4_5
import com.ankitt.themovieshow.core.database.migration.MIGRATION_5_6
import com.ankitt.themovieshow.core.database.migration.MIGRATION_6_7
import com.ankitt.themovieshow.core.database.movie.MovieDao
import com.ankitt.themovieshow.core.database.movie.MovieDetailDao
import com.ankitt.themovieshow.core.database.outbox.PendingOperationDao
import com.ankitt.themovieshow.core.database.recentlyviewed.RecentlyViewedDao
import com.ankitt.themovieshow.core.database.sync.SyncStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "themovieshow.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesTheMovieShowDatabase(
        @ApplicationContext context: Context,
    ): TheMovieShowDatabase = Room.databaseBuilder(
        context = context,
        klass = TheMovieShowDatabase::class.java,
        name = DATABASE_NAME,
    )
        // No fallbackToDestructiveMigration: this is the local source of truth for favorites,
        // watchlist and personal ratings, none of which TMDB can hand back to us. A destructive
        // fallback would silently delete user data on every schema change we forget to migrate
        // correctly, instead of failing loudly in development.
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
        .build()

    @Provides
    @Singleton
    fun providesMovieDao(database: TheMovieShowDatabase): MovieDao = database.movieDao()

    @Provides
    @Singleton
    fun providesSyncStateDao(database: TheMovieShowDatabase): SyncStateDao = database.syncStateDao()

    @Provides
    @Singleton
    fun providesMovieDetailDao(database: TheMovieShowDatabase): MovieDetailDao = database.movieDetailDao()

    @Provides
    @Singleton
    fun providesBookmarkDao(database: TheMovieShowDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    @Singleton
    fun providesRecentlyViewedDao(database: TheMovieShowDatabase): RecentlyViewedDao =
        database.recentlyViewedDao()

    @Provides
    @Singleton
    fun providesPendingOperationDao(database: TheMovieShowDatabase): PendingOperationDao =
        database.pendingOperationDao()
}

package com.ankitt.themovieshow.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds MovieEntity/GenreEntity/MovieListEntity (Phase 2's Home screen data). Hand-written rather
 * than a destructive fallback: see DatabaseModule's own comment on why this database never falls
 * back to destroying user data on a schema mismatch.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `movie` (
                `movieId` INTEGER NOT NULL,
                `title` TEXT NOT NULL,
                `overview` TEXT NOT NULL,
                `posterPath` TEXT,
                `backdropPath` TEXT,
                `releaseDate` TEXT,
                `voteAverage` REAL NOT NULL,
                `voteCount` INTEGER NOT NULL,
                `popularity` REAL NOT NULL,
                PRIMARY KEY(`movieId`)
            )""",
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `genre` (
                `genreId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                PRIMARY KEY(`genreId`)
            )""",
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `movie_list_membership` (
                `listKey` TEXT NOT NULL,
                `movieId` INTEGER NOT NULL,
                `position` INTEGER NOT NULL,
                PRIMARY KEY(`listKey`, `movieId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_movie_list_membership_movieId` ON `movie_list_membership` (`movieId`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_movie_list_membership_listKey_position` ON `movie_list_membership` (`listKey`, `position`)",
        )
    }
}

/** Adds MovieDetailEntity/MovieGenreCrossRef/MovieCastEntity for the Movie Detail screen. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `movie_detail` (
                `movieId` INTEGER NOT NULL,
                `runtime` INTEGER,
                `tagline` TEXT,
                `originalLanguage` TEXT,
                PRIMARY KEY(`movieId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `movie_genre_cross_ref` (
                `movieId` INTEGER NOT NULL,
                `genreId` INTEGER NOT NULL,
                PRIMARY KEY(`movieId`, `genreId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`genreId`) REFERENCES `genre`(`genreId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_movie_genre_cross_ref_genreId` ON `movie_genre_cross_ref` (`genreId`)",
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `movie_cast` (
                `movieId` INTEGER NOT NULL,
                `castId` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `character` TEXT NOT NULL,
                `profilePath` TEXT,
                `order` INTEGER NOT NULL,
                PRIMARY KEY(`movieId`, `castId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_movie_cast_movieId_order` ON `movie_cast` (`movieId`, `order`)",
        )
    }
}

/** Adds the YouTube trailer key to [movie_detail] for in-app trailer playback. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `movie_detail` ADD COLUMN `trailerYoutubeKey` TEXT")
    }
}

/** Adds the favorite_movie/watchlist_movie tables backing the Bookmarks feature. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `favorite_movie` (
                `movieId` INTEGER NOT NULL,
                `addedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`movieId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `watchlist_movie` (
                `movieId` INTEGER NOT NULL,
                `addedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`movieId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )
    }
}

/** Adds the recently_viewed table backing the Recently Viewed feature. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `recently_viewed` (
                `movieId` INTEGER NOT NULL,
                `viewedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`movieId`),
                FOREIGN KEY(`movieId`) REFERENCES `movie`(`movieId`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""",
        )
    }
}

/** Adds the pending_operation outbox table queuing offline mutations for Phase 6's sync worker. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `pending_operation` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `operationType` TEXT NOT NULL,
                `payload` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `retryCount` INTEGER NOT NULL,
                `lastAttemptEpochMillis` INTEGER,
                `lastErrorMessage` TEXT
            )""",
        )
    }
}

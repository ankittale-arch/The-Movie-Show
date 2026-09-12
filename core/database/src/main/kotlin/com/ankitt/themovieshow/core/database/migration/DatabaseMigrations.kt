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

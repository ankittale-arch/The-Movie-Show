package com.ankitt.themovieshow.core.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Round-trip tests for [DatabaseMigrations] using [MigrationTestHelper] against the schema JSON
 * snapshots committed under `core/database/schemas/` (wired in as a test asset dir in
 * build.gradle.kts). Each test creates the database at an old version, inserts data with that
 * version's raw SQL, runs the migration under test, and asserts both that the new schema matches
 * what Room expects (`runMigrationsAndValidate`'s job) and that the pre-migration data survived.
 *
 * NOTE: `schemas/…/6.json` is missing from the committed schema directory (only 1, 2, 3, 4, 5, 7
 * exist) — it looks like the version-6 export was never captured/committed before the database
 * moved on to version 7, and it can't be regenerated now since the `@Database` class is already
 * at version 7. That makes MIGRATION_6_7 impossible to validate in isolation (there's no version-6
 * schema to build/validate against), so `migrate5To7_survivesRecentlyViewedDataAndAddsPendingOperation`
 * below round-trips it as part of the 5→7 chain instead, validating the end state against 7.json.
 * If isolated coverage of 6_7 is wanted later, regenerate 6.json by temporarily checking out the
 * revision where the database was at version 6 and running a build there.
 */
@RunWith(RobolectricTestRunner::class)
class DatabaseMigrationsTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TheMovieShowDatabase::class.java,
    )

    @Test
    fun `migrate1To2 preserves sync_state data and creates movie, genre and membership tables`() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """INSERT INTO sync_state (resource, lastSyncedAtEpochMillis, isSyncing, lastErrorMessage)
                   VALUES ('popular', 1000, 0, NULL)""",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT resource, lastSyncedAtEpochMillis FROM sync_state").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("popular", cursor.getString(0))
            assertEquals(1000L, cursor.getLong(1))
        }

        // The new tables exist and are queryable (throws if they don't).
        db.query("SELECT * FROM movie").close()
        db.query("SELECT * FROM genre").close()
        db.query("SELECT * FROM movie_list_membership").close()
    }

    @Test
    fun `migrate2To3 preserves movie data and adds detail, genre cross-ref and cast tables`() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """INSERT INTO movie (movieId, title, overview, posterPath, backdropPath, releaseDate,
                   voteAverage, voteCount, popularity) VALUES (1, 'Inception', 'overview', NULL, NULL,
                   '2010-07-16', 8.8, 1000, 100.0)""",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        db.query("SELECT title FROM movie WHERE movieId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Inception", cursor.getString(0))
        }
        db.query("SELECT * FROM movie_detail").close()
        db.query("SELECT * FROM movie_genre_cross_ref").close()
        db.query("SELECT * FROM movie_cast").close()
    }

    @Test
    fun `migrate3To4 preserves movie_detail data and adds trailerYoutubeKey column`() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """INSERT INTO movie (movieId, title, overview, posterPath, backdropPath, releaseDate,
                   voteAverage, voteCount, popularity) VALUES (1, 'Inception', 'overview', NULL, NULL,
                   '2010-07-16', 8.8, 1000, 100.0)""",
            )
            execSQL(
                """INSERT INTO movie_detail (movieId, runtime, tagline, originalLanguage)
                   VALUES (1, 148, 'Your mind is the scene of the crime', 'en')""",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        db.query("SELECT tagline, trailerYoutubeKey FROM movie_detail WHERE movieId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Your mind is the scene of the crime", cursor.getString(0))
            assertTrue(cursor.isNull(1))
        }
    }

    @Test
    fun `migrate4To5 preserves movie data and adds favorite and watchlist tables`() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """INSERT INTO movie (movieId, title, overview, posterPath, backdropPath, releaseDate,
                   voteAverage, voteCount, popularity) VALUES (1, 'Inception', 'overview', NULL, NULL,
                   '2010-07-16', 8.8, 1000, 100.0)""",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        db.query("SELECT title FROM movie WHERE movieId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Inception", cursor.getString(0))
        }
        db.query("SELECT * FROM favorite_movie").close()
        db.query("SELECT * FROM watchlist_movie").close()
    }

    /**
     * Covers MIGRATION_5_6 and MIGRATION_6_7 together in one chain (see the class doc comment for
     * why 6_7 can't be isolated): data inserted at version 5 — before either migration runs — must
     * still be there after both have applied, and the tables each migration adds
     * (`recently_viewed`, `pending_operation`) must exist in the final, Room-validated schema.
     */
    @Test
    fun `migrate5To7 preserves movie data and adds recently_viewed and pending_operation tables`() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """INSERT INTO movie (movieId, title, overview, posterPath, backdropPath, releaseDate,
                   voteAverage, voteCount, popularity) VALUES (1, 'Inception', 'overview', NULL, NULL,
                   '2010-07-16', 8.8, 1000, 100.0)""",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_5_6, MIGRATION_6_7)

        db.query("SELECT title FROM movie WHERE movieId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Inception", cursor.getString(0))
        }
        // Both new tables exist post-migration and accept an insert (throws if the schema is wrong).
        db.execSQL("INSERT INTO recently_viewed (movieId, viewedAtEpochMillis) VALUES (1, 2000)")
        db.execSQL(
            """INSERT INTO pending_operation (operationType, payload, createdAtEpochMillis, retryCount)
               VALUES ('TOGGLE_FAVORITE', '{}', 3000, 0)""",
        )
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}

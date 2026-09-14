package com.ankitt.themovieshow.core.database.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises [SyncStateDao] against a real in-memory Room database via Robolectric — see
 * MovieDaoTest's doc comment for why this doesn't mock Room.
 */
@RunWith(RobolectricTestRunner::class)
class SyncStateDaoTest {

    private lateinit var database: TheMovieShowDatabase
    private lateinit var dao: SyncStateDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheMovieShowDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.syncStateDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `upsert then get returns the stored sync state`() = runTest {
        dao.upsert(
            SyncStateEntity(
                resource = "popular",
                lastSyncedAtEpochMillis = 1_000L,
                isSyncing = false,
                lastErrorMessage = null,
            ),
        )

        val result = dao.get("popular")
        assertEquals(1_000L, result?.lastSyncedAtEpochMillis)
        assertEquals(false, result?.isSyncing)
    }

    @Test
    fun `upsert overwrites an existing row for the same resource`() = runTest {
        dao.upsert(
            SyncStateEntity(
                resource = "popular",
                lastSyncedAtEpochMillis = 1_000L,
                isSyncing = true,
                lastErrorMessage = null,
            ),
        )
        dao.upsert(
            SyncStateEntity(
                resource = "popular",
                lastSyncedAtEpochMillis = 2_000L,
                isSyncing = false,
                lastErrorMessage = "timeout",
            ),
        )

        val result = dao.get("popular")
        assertEquals(2_000L, result?.lastSyncedAtEpochMillis)
        assertEquals(false, result?.isSyncing)
        assertEquals("timeout", result?.lastErrorMessage)
    }

    @Test
    fun `get returns null for a resource that has never synced`() = runTest {
        assertNull(dao.get("never_synced"))
    }

    @Test
    fun `observe emits null for a resource that has never synced`() = runTest {
        dao.observe("never_synced").test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe emits the stored sync state after upsert`() = runTest {
        dao.upsert(
            SyncStateEntity(
                resource = "trending",
                lastSyncedAtEpochMillis = 5_000L,
                isSyncing = false,
                lastErrorMessage = null,
            ),
        )

        dao.observe("trending").test {
            assertEquals(5_000L, awaitItem()?.lastSyncedAtEpochMillis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe is scoped to its own resource`() = runTest {
        dao.upsert(
            SyncStateEntity(
                resource = "popular",
                lastSyncedAtEpochMillis = 1_000L,
                isSyncing = false,
                lastErrorMessage = null,
            ),
        )
        dao.upsert(
            SyncStateEntity(
                resource = "trending",
                lastSyncedAtEpochMillis = 2_000L,
                isSyncing = false,
                lastErrorMessage = null,
            ),
        )

        dao.observe("popular").test {
            assertEquals(1_000L, awaitItem()?.lastSyncedAtEpochMillis)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

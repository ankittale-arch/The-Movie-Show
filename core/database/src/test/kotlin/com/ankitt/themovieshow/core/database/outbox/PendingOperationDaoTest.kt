package com.ankitt.themovieshow.core.database.outbox

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises [PendingOperationDao] against a real in-memory Room database via Robolectric — see
 * MovieDaoTest's doc comment for why this doesn't mock Room. [PendingOperationEntity] has no
 * foreign key to a movie row (see its own doc comment), so rows are self-contained here.
 */
@RunWith(RobolectricTestRunner::class)
class PendingOperationDaoTest {

    private lateinit var database: TheMovieShowDatabase
    private lateinit var dao: PendingOperationDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheMovieShowDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.pendingOperationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun operation(
        type: String = "TOGGLE_FAVORITE",
        payload: String = "{\"movieId\":1}",
        createdAt: Long = 1_000L,
    ) = PendingOperationEntity(
        operationType = type,
        payload = payload,
        createdAtEpochMillis = createdAt,
    )

    @Test
    fun `enqueue returns the generated id`() = runTest {
        val id = dao.enqueue(operation())

        assertTrue(id > 0)
        assertEquals(1, dao.getAllPending().size)
    }

    @Test
    fun `getAllPending orders operations by createdAtEpochMillis ascending`() = runTest {
        dao.enqueue(operation(createdAt = 3_000L))
        dao.enqueue(operation(createdAt = 1_000L))
        dao.enqueue(operation(createdAt = 2_000L))

        val pending = dao.getAllPending()
        assertEquals(listOf(1_000L, 2_000L, 3_000L), pending.map { it.createdAtEpochMillis })
    }

    @Test
    fun `observePendingOperations orders operations by createdAtEpochMillis ascending`() = runTest {
        dao.enqueue(operation(createdAt = 3_000L))
        dao.enqueue(operation(createdAt = 1_000L))
        dao.enqueue(operation(createdAt = 2_000L))

        dao.observePendingOperations().test {
            assertEquals(listOf(1_000L, 2_000L, 3_000L), awaitItem().map { it.createdAtEpochMillis })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete removes the operation with the given id`() = runTest {
        val id = dao.enqueue(operation())

        dao.delete(id)

        assertTrue(dao.getAllPending().isEmpty())
    }

    @Test
    fun `recordFailedAttempt increments retryCount and sets lastAttempt fields`() = runTest {
        val id = dao.enqueue(operation())

        dao.recordFailedAttempt(id, attemptedAtEpochMillis = 5_000L, errorMessage = "network error")

        val updated = dao.getAllPending().single()
        assertEquals(1, updated.retryCount)
        assertEquals(5_000L, updated.lastAttemptEpochMillis)
        assertEquals("network error", updated.lastErrorMessage)
    }

    @Test
    fun `recordFailedAttempt accumulates retryCount across multiple failures`() = runTest {
        val id = dao.enqueue(operation())

        dao.recordFailedAttempt(id, attemptedAtEpochMillis = 5_000L, errorMessage = "first error")
        dao.recordFailedAttempt(id, attemptedAtEpochMillis = 6_000L, errorMessage = "second error")

        val updated = dao.getAllPending().single()
        assertEquals(2, updated.retryCount)
        assertEquals(6_000L, updated.lastAttemptEpochMillis)
        assertEquals("second error", updated.lastErrorMessage)
    }

    @Test
    fun `recordFailedAttempt accepts a null error message`() = runTest {
        val id = dao.enqueue(operation())

        dao.recordFailedAttempt(id, attemptedAtEpochMillis = 5_000L, errorMessage = null)

        assertNull(dao.getAllPending().single().lastErrorMessage)
    }

    @Test
    fun `clearAll removes every pending operation`() = runTest {
        dao.enqueue(operation(createdAt = 1_000L))
        dao.enqueue(operation(createdAt = 2_000L))

        dao.clearAll()

        assertTrue(dao.getAllPending().isEmpty())
    }
}

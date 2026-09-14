package com.ankitt.themovieshow.core.database.recentlyviewed

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import com.ankitt.themovieshow.core.database.movie.MovieDao
import com.ankitt.themovieshow.core.database.movie.MovieEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises [RecentlyViewedDao] against a real in-memory Room database via Robolectric — see
 * MovieDaoTest's doc comment for why this doesn't mock Room. [RecentlyViewedEntity] carries a
 * CASCADE foreign key to [MovieEntity], so every test upserts the parent movie first via
 * [MovieDao].
 */
@RunWith(RobolectricTestRunner::class)
class RecentlyViewedDaoTest {

    private lateinit var database: TheMovieShowDatabase
    private lateinit var dao: RecentlyViewedDao
    private lateinit var movieDao: MovieDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheMovieShowDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.recentlyViewedDao()
        movieDao = database.movieDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun movie(id: Int, title: String = "Movie $id") = MovieEntity(
        movieId = id,
        title = title,
        overview = "overview",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2024-01-01",
        voteAverage = 7.5,
        voteCount = 100,
        popularity = 42.0,
    )

    @Test
    fun `upsertRecentlyViewed makes a movie observable in the recently viewed list`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))

        dao.observeRecentlyViewedMovies(limit = 10).test {
            assertEquals(listOf(1), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeRecentlyViewedMovies orders most recently viewed first`() = runTest {
        movieDao.upsertMovies(listOf(movie(1), movie(2), movie(3)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 2, viewedAtEpochMillis = 3_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 3, viewedAtEpochMillis = 2_000L))

        dao.observeRecentlyViewedMovies(limit = 10).test {
            assertEquals(listOf(2, 3, 1), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeRecentlyViewedMovies respects the limit param`() = runTest {
        movieDao.upsertMovies(listOf(movie(1), movie(2), movie(3)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 2, viewedAtEpochMillis = 2_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 3, viewedAtEpochMillis = 3_000L))

        dao.observeRecentlyViewedMovies(limit = 2).test {
            assertEquals(listOf(3, 2), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `upsertRecentlyViewed updates the viewedAt timestamp of an existing row`() = runTest {
        movieDao.upsertMovies(listOf(movie(1), movie(2)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 2, viewedAtEpochMillis = 2_000L))

        // Re-viewing movie 1 should bump it back to the front.
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 3_000L))

        dao.observeRecentlyViewedMovies(limit = 10).test {
            assertEquals(listOf(1, 2), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `trimTo keeps only the N most recently viewed rows and drops the rest`() = runTest {
        movieDao.upsertMovies(listOf(movie(1), movie(2), movie(3), movie(4)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 2, viewedAtEpochMillis = 2_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 3, viewedAtEpochMillis = 3_000L))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 4, viewedAtEpochMillis = 4_000L))

        dao.trimTo(2)

        dao.observeRecentlyViewedMovies(limit = 10).test {
            assertEquals(listOf(4, 3), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearRecentlyViewed removes all rows`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))

        dao.clearRecentlyViewed()

        dao.observeRecentlyViewedMovies(limit = 10).test {
            assertEquals(emptyList<Int>(), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting the parent movie cascades to remove the recently viewed row`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        dao.upsertRecentlyViewed(RecentlyViewedEntity(movieId = 1, viewedAtEpochMillis = 1_000L))

        database.openHelper.writableDatabase.execSQL("DELETE FROM movie WHERE movieId = 1")

        dao.observeRecentlyViewedMovies(limit = 10).test {
            assertEquals(emptyList<Int>(), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }
}

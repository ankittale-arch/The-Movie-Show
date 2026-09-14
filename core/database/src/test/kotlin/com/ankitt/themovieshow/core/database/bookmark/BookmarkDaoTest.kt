package com.ankitt.themovieshow.core.database.bookmark

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import com.ankitt.themovieshow.core.database.movie.MovieDao
import com.ankitt.themovieshow.core.database.movie.MovieEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises [BookmarkDao] against a real in-memory Room database via Robolectric — see
 * MovieDaoTest's doc comment for why this doesn't mock Room. Favorite/watchlist rows carry a
 * CASCADE foreign key to [MovieEntity], so every test upserts the parent movie first via
 * [MovieDao].
 */
@RunWith(RobolectricTestRunner::class)
class BookmarkDaoTest {

    private lateinit var database: TheMovieShowDatabase
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var movieDao: MovieDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheMovieShowDatabase::class.java,
        ).allowMainThreadQueries().build()
        bookmarkDao = database.bookmarkDao()
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
    fun `upsertFavorite makes a movie observable as favorite`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        bookmarkDao.upsertFavorite(FavoriteMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))

        assertTrue(bookmarkDao.isFavoriteOnce(1))
        bookmarkDao.observeIsFavorite(1).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteFavorite removes the favorite row`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        bookmarkDao.upsertFavorite(FavoriteMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))

        bookmarkDao.deleteFavorite(1)

        assertFalse(bookmarkDao.isFavoriteOnce(1))
        bookmarkDao.observeIsFavorite(1).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeFavoriteMovies returns movies most-recently-added first`() = runTest {
        movieDao.upsertMovies(listOf(movie(1), movie(2)))
        bookmarkDao.upsertFavorite(FavoriteMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))
        bookmarkDao.upsertFavorite(FavoriteMovieEntity(movieId = 2, addedAtEpochMillis = 2_000L))

        bookmarkDao.observeFavoriteMovies().test {
            assertEquals(listOf(2, 1), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting the parent movie cascades to remove the favorite row`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        bookmarkDao.upsertFavorite(FavoriteMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))

        database.openHelper.writableDatabase.execSQL("DELETE FROM movie WHERE movieId = 1")

        assertFalse(bookmarkDao.isFavoriteOnce(1))
    }

    @Test
    fun `upsertWatchlist makes a movie observable as in watchlist`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        bookmarkDao.upsertWatchlist(WatchlistMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))

        assertTrue(bookmarkDao.isInWatchlistOnce(1))
        bookmarkDao.observeIsInWatchlist(1).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteWatchlist removes the watchlist row`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        bookmarkDao.upsertWatchlist(WatchlistMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))

        bookmarkDao.deleteWatchlist(1)

        assertFalse(bookmarkDao.isInWatchlistOnce(1))
        bookmarkDao.observeIsInWatchlist(1).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeWatchlistMovies returns movies most-recently-added first`() = runTest {
        movieDao.upsertMovies(listOf(movie(1), movie(2)))
        bookmarkDao.upsertWatchlist(WatchlistMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))
        bookmarkDao.upsertWatchlist(WatchlistMovieEntity(movieId = 2, addedAtEpochMillis = 2_000L))

        bookmarkDao.observeWatchlistMovies().test {
            assertEquals(listOf(2, 1), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting the parent movie cascades to remove the watchlist row`() = runTest {
        movieDao.upsertMovies(listOf(movie(1)))
        bookmarkDao.upsertWatchlist(WatchlistMovieEntity(movieId = 1, addedAtEpochMillis = 1_000L))

        database.openHelper.writableDatabase.execSQL("DELETE FROM movie WHERE movieId = 1")

        assertFalse(bookmarkDao.isInWatchlistOnce(1))
    }
}

package com.ankitt.themovieshow.core.database.movie

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.ankitt.themovieshow.core.database.TheMovieShowDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Exercises [MovieDao] against a real (in-memory) SQLite database via Robolectric, rather than
 * mocking Room — the behavior worth verifying here (the [MovieDao.replaceListMembership]
 * transaction, cascading deletes, join/order-by queries) all lives in SQL and Room-generated
 * code, not in Kotlin this project owns. The SDK Robolectric emulates is pinned in
 * `src/test/resources/robolectric.properties` (this module's compileSdk is newer than
 * Robolectric 4.15.1 ships shadows for).
 */
@RunWith(RobolectricTestRunner::class)
class MovieDaoTest {

    private lateinit var database: TheMovieShowDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheMovieShowDatabase::class.java,
        ).allowMainThreadQueries().build()
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
    fun `replaceListMembership makes the new movies observable in position order`() = runTest {
        movieDao.replaceListMembership(
            listKey = "popular",
            movies = listOf(movie(1), movie(2)),
            memberships = listOf(
                MovieListEntity(listKey = "popular", movieId = 2, position = 0),
                MovieListEntity(listKey = "popular", movieId = 1, position = 1),
            ),
        )

        val result = movieDao.observeMoviesForList("popular").test {
            val movies = awaitItem()
            assertEquals(listOf(2, 1), movies.map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceListMembership clears prior membership for that list key only`() = runTest {
        movieDao.replaceListMembership(
            listKey = "popular",
            movies = listOf(movie(1), movie(2)),
            memberships = listOf(
                MovieListEntity(listKey = "popular", movieId = 1, position = 0),
                MovieListEntity(listKey = "popular", movieId = 2, position = 1),
            ),
        )

        // Replacing again with just movie 1 should drop movie 2 from "popular".
        movieDao.replaceListMembership(
            listKey = "popular",
            movies = listOf(movie(1)),
            memberships = listOf(MovieListEntity(listKey = "popular", movieId = 1, position = 0)),
        )

        movieDao.observeMoviesForList("popular").test {
            assertEquals(listOf(1), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `appendListMembership adds a page after existing membership without clearing it`() = runTest {
        movieDao.replaceListMembership(
            listKey = "popular",
            movies = listOf(movie(1)),
            memberships = listOf(MovieListEntity(listKey = "popular", movieId = 1, position = 0)),
        )

        movieDao.appendListMembership(
            movies = listOf(movie(2)),
            memberships = listOf(MovieListEntity(listKey = "popular", movieId = 2, position = 1)),
        )

        assertEquals(2, movieDao.countListMembership("popular"))
        movieDao.observeMoviesForList("popular").test {
            assertEquals(listOf(1, 2), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeMoviesForList is scoped to its own list key`() = runTest {
        movieDao.replaceListMembership(
            listKey = "popular",
            movies = listOf(movie(1)),
            memberships = listOf(MovieListEntity(listKey = "popular", movieId = 1, position = 0)),
        )
        movieDao.replaceListMembership(
            listKey = "trending",
            movies = listOf(movie(2)),
            memberships = listOf(MovieListEntity(listKey = "trending", movieId = 2, position = 0)),
        )

        movieDao.observeMoviesForList("popular").test {
            assertEquals(listOf(1), awaitItem().map { it.movieId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeGenres returns genres alphabetically`() = runTest {
        movieDao.upsertGenres(
            listOf(
                GenreEntity(genreId = 1, name = "Thriller"),
                GenreEntity(genreId = 2, name = "Action"),
            ),
        )

        movieDao.observeGenres().test {
            assertEquals(listOf("Action", "Thriller"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `upsertMovies overwrites an existing row with the same primary key`() = runTest {
        movieDao.upsertMovies(listOf(movie(1, title = "Original")))
        movieDao.upsertMovies(listOf(movie(1, title = "Updated")))

        movieDao.replaceListMembership(
            listKey = "popular",
            movies = emptyList(),
            memberships = listOf(MovieListEntity(listKey = "popular", movieId = 1, position = 0)),
        )

        movieDao.observeMoviesForList("popular").test {
            assertEquals("Updated", awaitItem().single().title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

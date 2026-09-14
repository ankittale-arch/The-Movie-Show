package com.ankitt.themovieshow.core.database.movie

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
 * Exercises [MovieDetailDao] against a real in-memory Room database via Robolectric — see
 * MovieDaoTest's doc comment for why this doesn't mock Room. Focus areas: the
 * [MovieDetailDao.replaceMovieDetail] atomic transaction (movie + detail + genres + genre
 * cross-refs + cast all written together, with cross-refs/cast cleared before each re-insert) and
 * the observe* join/order-by queries.
 */
@RunWith(RobolectricTestRunner::class)
class MovieDetailDaoTest {

    private lateinit var database: TheMovieShowDatabase
    private lateinit var dao: MovieDetailDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheMovieShowDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.movieDetailDao()
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

    private fun detail(id: Int, tagline: String = "tagline") = MovieDetailEntity(
        movieId = id,
        runtime = 120,
        tagline = tagline,
        originalLanguage = "en",
        trailerYoutubeKey = "abc123",
    )

    @Test
    fun `replaceMovieDetail writes movie, detail, genres, cross-refs and cast atomically`() = runTest {
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = listOf(GenreEntity(genreId = 1, name = "Action"), GenreEntity(genreId = 2, name = "Comedy")),
            genreCrossRefs = listOf(
                MovieGenreCrossRef(movieId = 1, genreId = 1),
                MovieGenreCrossRef(movieId = 1, genreId = 2),
            ),
            cast = listOf(
                MovieCastEntity(movieId = 1, castId = 10, name = "Actor A", character = "Hero", profilePath = null, order = 0),
                MovieCastEntity(movieId = 1, castId = 11, name = "Actor B", character = "Villain", profilePath = null, order = 1),
            ),
        )

        dao.observeMovie(1).test {
            assertEquals("Movie 1", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
        dao.observeMovieDetail(1).test {
            assertEquals("tagline", awaitItem()?.tagline)
            cancelAndIgnoreRemainingEvents()
        }
        dao.observeGenresForMovie(1).test {
            assertEquals(listOf("Action", "Comedy"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
        dao.observeCastForMovie(1).test {
            assertEquals(listOf("Actor A", "Actor B"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeGenresForMovie orders genres alphabetically by name`() = runTest {
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = listOf(GenreEntity(genreId = 1, name = "Thriller"), GenreEntity(genreId = 2, name = "Action")),
            genreCrossRefs = listOf(
                MovieGenreCrossRef(movieId = 1, genreId = 1),
                MovieGenreCrossRef(movieId = 1, genreId = 2),
            ),
            cast = emptyList(),
        )

        dao.observeGenresForMovie(1).test {
            assertEquals(listOf("Action", "Thriller"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCastForMovie orders cast by order ascending`() = runTest {
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = emptyList(),
            genreCrossRefs = emptyList(),
            cast = listOf(
                MovieCastEntity(movieId = 1, castId = 20, name = "Second", character = "b", profilePath = null, order = 1),
                MovieCastEntity(movieId = 1, castId = 21, name = "First", character = "a", profilePath = null, order = 0),
            ),
        )

        dao.observeCastForMovie(1).test {
            assertEquals(listOf("First", "Second"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceMovieDetail clears prior genre cross-refs before inserting the new ones`() = runTest {
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = listOf(GenreEntity(genreId = 1, name = "Action"), GenreEntity(genreId = 2, name = "Comedy")),
            genreCrossRefs = listOf(
                MovieGenreCrossRef(movieId = 1, genreId = 1),
                MovieGenreCrossRef(movieId = 1, genreId = 2),
            ),
            cast = emptyList(),
        )

        // Second call only references genre 1 — the stale cross-ref to genre 2 must be gone.
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = listOf(GenreEntity(genreId = 1, name = "Action")),
            genreCrossRefs = listOf(MovieGenreCrossRef(movieId = 1, genreId = 1)),
            cast = emptyList(),
        )

        dao.observeGenresForMovie(1).test {
            assertEquals(listOf("Action"), awaitItem().map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceMovieDetail clears prior cast before inserting the new cast`() = runTest {
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = emptyList(),
            genreCrossRefs = emptyList(),
            cast = listOf(
                MovieCastEntity(movieId = 1, castId = 10, name = "Actor A", character = "Hero", profilePath = null, order = 0),
                MovieCastEntity(movieId = 1, castId = 11, name = "Actor B", character = "Villain", profilePath = null, order = 1),
            ),
        )

        // Second call drops castId 11 entirely — it must not linger from the first insert.
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1),
            genres = emptyList(),
            genreCrossRefs = emptyList(),
            cast = listOf(
                MovieCastEntity(movieId = 1, castId = 10, name = "Actor A", character = "Hero", profilePath = null, order = 0),
            ),
        )

        dao.observeCastForMovie(1).test {
            assertEquals(listOf(10), awaitItem().map { it.castId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replaceMovieDetail overwrites the detail row's tagline on a second call`() = runTest {
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1, tagline = "Original"),
            genres = emptyList(),
            genreCrossRefs = emptyList(),
            cast = emptyList(),
        )
        dao.replaceMovieDetail(
            movie = movie(1),
            detail = detail(1, tagline = "Updated"),
            genres = emptyList(),
            genreCrossRefs = emptyList(),
            cast = emptyList(),
        )

        dao.observeMovieDetail(1).test {
            assertEquals("Updated", awaitItem()?.tagline)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeMovie and observeMovieDetail emit null for an unknown movie id`() = runTest {
        dao.observeMovie(999).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        dao.observeMovieDetail(999).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

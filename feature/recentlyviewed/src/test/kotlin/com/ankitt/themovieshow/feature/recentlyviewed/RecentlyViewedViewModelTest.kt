package com.ankitt.themovieshow.feature.recentlyviewed

import app.cash.turbine.test
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.model.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecentlyViewedViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()
    private val recentlyViewedFlow = MutableStateFlow<List<Movie>>(emptyList())

    private fun movie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        overview = "overview",
        posterPath = null,
        backdropPath = null,
        releaseDate = null,
        voteAverage = 5.0,
        voteCount = 10,
        popularity = 1.0,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeRecentlyViewedMovies() } returns recentlyViewedFlow
        coEvery { repository.clearRecentlyViewed() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = RecentlyViewedViewModel(repository)

    @Test
    fun `initial state is empty`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            assertEquals(emptyList<RecentlyViewedItem>(), awaitItem().movies)
        }
    }

    @Test
    fun `state reflects repository's recently viewed movies`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            recentlyViewedFlow.value = listOf(movie(1), movie(2))

            val state = awaitItem()
            assertEquals(2, state.movies.size)
            assertEquals("Movie 1", state.movies[0].title)
        }
    }

    @Test
    fun `clearHistory delegates to repository`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.clearHistory()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.clearRecentlyViewed() }
    }
}

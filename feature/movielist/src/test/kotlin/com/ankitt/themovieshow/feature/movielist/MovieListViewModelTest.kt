package com.ankitt.themovieshow.feature.movielist

import app.cash.turbine.test
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.model.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()

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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = MovieListViewModel(repository)

    @Test
    fun `initial state is empty`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<MovieListItem>(), state.movies)
            assertFalse(state.isLoading)
            assertFalse(state.isLoadingMore)
            assertTrue(state.hasMorePages)
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun `load triggers refresh and populates from observed movies`() = runTest(dispatcher) {
        val moviesFlow = MutableStateFlow(listOf(movie(1), movie(2)))
        coEvery { repository.refreshMoviesForList("popular") } coAnswers {
            delay(1)
            Result.success(Unit)
        }
        every { repository.observeMoviesForList("popular") } returns moviesFlow

        val vm = viewModel()
        vm.uiState.test {
            assertEquals(emptyList<MovieListItem>(), awaitItem().movies)

            vm.load("popular")

            // loading = true
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            dispatcher.scheduler.advanceUntilIdle()

            val loadedState = awaitItem()
            assertFalse(loadedState.isLoading)
            assertEquals(2, loadedState.movies.size)
            assertEquals("Movie 1", loadedState.movies[0].title)
        }
        coVerify(exactly = 1) { repository.refreshMoviesForList("popular") }
    }

    @Test
    fun `load called twice with same key only refreshes once`() = runTest(dispatcher) {
        val moviesFlow = MutableStateFlow(listOf(movie(1)))
        coEvery { repository.refreshMoviesForList("popular") } returns Result.success(Unit)
        every { repository.observeMoviesForList("popular") } returns moviesFlow

        val vm = viewModel()
        vm.load("popular")
        dispatcher.scheduler.advanceUntilIdle()
        vm.load("popular")
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.refreshMoviesForList("popular") }
    }

    @Test
    fun `loadMore advances page and appends`() = runTest(dispatcher) {
        val moviesFlow = MutableStateFlow(listOf(movie(1)))
        coEvery { repository.refreshMoviesForList("popular") } returns Result.success(Unit)
        coEvery { repository.loadMoreMoviesForList("popular", 2) } coAnswers {
            delay(1)
            moviesFlow.value = moviesFlow.value + movie(2)
            Result.success(true)
        }
        every { repository.observeMoviesForList("popular") } returns moviesFlow

        val vm = viewModel()
        vm.load("popular")
        dispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            skipItems(1) // the eager stateIn initialValue, before combine recomputes
            val initial = awaitItem()
            assertEquals(1, initial.movies.size)

            vm.loadMore()

            // isLoadingMore true, then settle once loadMoreMoviesForList resolves
            var state = awaitItem()
            assertTrue(state.isLoadingMore)

            dispatcher.scheduler.advanceUntilIdle()

            state = awaitItem()
            while (state.isLoadingMore || state.movies.size != 2) {
                state = awaitItem()
            }
            assertEquals(2, state.movies.size)
            assertTrue(state.hasMorePages)
        }
        coVerify(exactly = 1) { repository.loadMoreMoviesForList("popular", 2) }
    }

    @Test
    fun `loadMore failure sets errorMessage`() = runTest(dispatcher) {
        val moviesFlow = MutableStateFlow(listOf(movie(1)))
        coEvery { repository.refreshMoviesForList("popular") } returns Result.success(Unit)
        coEvery { repository.loadMoreMoviesForList("popular", 2) } returns
            Result.failure(RuntimeException("boom"))
        every { repository.observeMoviesForList("popular") } returns moviesFlow

        val vm = viewModel()
        vm.load("popular")
        dispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            skipItems(1)
            vm.loadMore()
            dispatcher.scheduler.advanceUntilIdle()

            var state = awaitItem()
            while (state.errorMessage == null) {
                state = awaitItem()
            }
            assertEquals("boom", state.errorMessage)
        }
    }

    @Test
    fun `loadMore no-ops when hasMorePages is false`() = runTest(dispatcher) {
        val moviesFlow = MutableStateFlow(listOf(movie(1)))
        coEvery { repository.refreshMoviesForList("popular") } returns Result.success(Unit)
        coEvery { repository.loadMoreMoviesForList("popular", 2) } returns Result.success(false)
        every { repository.observeMoviesForList("popular") } returns moviesFlow

        val vm = viewModel()
        vm.load("popular")
        dispatcher.scheduler.advanceUntilIdle()
        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()
        // hasMorePages is now false
        vm.loadMore()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.loadMoreMoviesForList("popular", 2) }
    }
}

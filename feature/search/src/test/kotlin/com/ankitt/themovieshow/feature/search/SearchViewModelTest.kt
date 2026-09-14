package com.ankitt.themovieshow.feature.search

import app.cash.turbine.test
import com.ankitt.themovieshow.core.data.HomeListKeys
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()
    private val searchResultsFlow = MutableStateFlow<List<Movie>>(emptyList())

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
        every { repository.observeMoviesForList(HomeListKeys.SEARCH) } returns searchResultsFlow
        coEvery { repository.clearSearchResults() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SearchViewModel(repository)

    @Test
    fun `initial state is blank query with no results`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            val state = awaitItem()
            assertEquals("", state.query)
            assertEquals(emptyList<SearchResultMovie>(), state.results)
            assertFalse(state.isSearching)
            assertEquals(true, state.showPrompt)
        }
    }

    @Test
    fun `query below min length clears results without calling searchMovies`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onQueryChanged("a")
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.searchMovies(any()) }
        coVerify(exactly = 1) { repository.clearSearchResults() }
    }

    @Test
    fun `query change debounces before triggering search`() = runTest(dispatcher) {
        coEvery { repository.searchMovies("batman") } coAnswers {
            searchResultsFlow.value = listOf(movie(1))
            Result.success(Unit)
        }

        val vm = viewModel()
        vm.onQueryChanged("batman")

        // not yet debounced
        dispatcher.scheduler.advanceTimeBy(100)
        coVerify(exactly = 0) { repository.searchMovies(any()) }

        dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repository.searchMovies("batman") }
    }

    @Test
    fun `rapid keystrokes within debounce window only search once for final query`() = runTest(dispatcher) {
        coEvery { repository.searchMovies(any()) } returns Result.success(Unit)

        val vm = viewModel()
        vm.onQueryChanged("b")
        dispatcher.scheduler.advanceTimeBy(100)
        vm.onQueryChanged("ba")
        dispatcher.scheduler.advanceTimeBy(100)
        vm.onQueryChanged("bat")
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.searchMovies("bat") }
        coVerify(exactly = 0) { repository.searchMovies("b") }
        coVerify(exactly = 0) { repository.searchMovies("ba") }
    }

    @Test
    fun `search success populates results`() = runTest(dispatcher) {
        coEvery { repository.searchMovies("batman") } coAnswers {
            searchResultsFlow.value = listOf(movie(1), movie(2))
            Result.success(Unit)
        }

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onQueryChanged("batman")
            dispatcher.scheduler.advanceUntilIdle()

            var state = awaitItem()
            while (state.results.isEmpty()) {
                state = awaitItem()
            }
            assertEquals(2, state.results.size)
            assertFalse(state.isSearching)
        }
    }

    @Test
    fun `search failure sets errorMessage`() = runTest(dispatcher) {
        coEvery { repository.searchMovies("batman") } returns Result.failure(RuntimeException("boom"))

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.onQueryChanged("batman")
            dispatcher.scheduler.advanceUntilIdle()

            var state = awaitItem()
            while (state.errorMessage == null) {
                state = awaitItem()
            }
            assertEquals("boom", state.errorMessage)
            assertFalse(state.isSearching)
        }
    }

    @Test
    fun `clearing query back to blank clears search results`() = runTest(dispatcher) {
        coEvery { repository.searchMovies("batman") } returns Result.success(Unit)

        val vm = viewModel()
        vm.onQueryChanged("batman")
        dispatcher.scheduler.advanceUntilIdle()

        vm.onQueryChanged("")
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.clearSearchResults() }
    }
}

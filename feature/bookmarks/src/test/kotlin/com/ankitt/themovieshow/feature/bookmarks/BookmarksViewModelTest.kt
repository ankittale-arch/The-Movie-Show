package com.ankitt.themovieshow.feature.bookmarks

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
class BookmarksViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()
    private val favoritesFlow = MutableStateFlow<List<Movie>>(emptyList())
    private val watchlistFlow = MutableStateFlow<List<Movie>>(emptyList())

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
        every { repository.observeFavoriteMovies() } returns favoritesFlow
        every { repository.observeWatchlistMovies() } returns watchlistFlow
        coEvery { repository.toggleFavorite(any()) } returns Unit
        coEvery { repository.toggleWatchlist(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = BookmarksViewModel(repository)

    @Test
    fun `initial state is empty`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(emptyList<BookmarkItem>(), state.favorites)
            assertEquals(emptyList<BookmarkItem>(), state.watchlist)
        }
    }

    @Test
    fun `state reflects favorites and watchlist from repository`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            favoritesFlow.value = listOf(movie(1))
            watchlistFlow.value = listOf(movie(2), movie(3))

            var state = awaitItem()
            while (state.favorites.isEmpty() || state.watchlist.size != 2) {
                state = awaitItem()
            }
            assertEquals(1, state.favorites.size)
            assertEquals(2, state.watchlist.size)
        }
    }

    @Test
    fun `removeFavorite toggles favorite via repository`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.removeFavorite(7)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.toggleFavorite(7) }
    }

    @Test
    fun `removeFromWatchlist toggles watchlist via repository`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.removeFromWatchlist(9)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.toggleWatchlist(9) }
    }
}

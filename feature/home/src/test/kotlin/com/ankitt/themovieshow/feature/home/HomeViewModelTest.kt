package com.ankitt.themovieshow.feature.home

import app.cash.turbine.test
import com.ankitt.themovieshow.core.common.network.ConnectivityObserver
import com.ankitt.themovieshow.core.data.HomeListKeys
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.model.Genre
import com.ankitt.themovieshow.core.data.model.Movie
import com.ankitt.themovieshow.core.data.model.SyncMetadata
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()
    private val connectivityObserver = mockk<ConnectivityObserver>()
    private val onlineFlow = MutableStateFlow(true)

    private val trendingFlow = MutableStateFlow<List<Movie>>(emptyList())
    private val nowPlayingFlow = MutableStateFlow<List<Movie>>(emptyList())
    private val popularFlow = MutableStateFlow<List<Movie>>(emptyList())
    private val discoverFlow = MutableStateFlow<List<Movie>>(emptyList())
    private val upcomingFlow = MutableStateFlow<List<Movie>>(emptyList())
    private val genresFlow = MutableStateFlow<List<Genre>>(emptyList())
    private val syncMetadataFlow = MutableStateFlow(SyncMetadata(null, false, null))

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
        every { connectivityObserver.isOnline } returns onlineFlow
        every { repository.observeMoviesForList(HomeListKeys.TRENDING) } returns trendingFlow
        every { repository.observeMoviesForList(HomeListKeys.NOW_PLAYING) } returns nowPlayingFlow
        every { repository.observeMoviesForList(HomeListKeys.POPULAR) } returns popularFlow
        every { repository.observeMoviesForList(HomeListKeys.DISCOVER) } returns discoverFlow
        every { repository.observeMoviesForList(HomeListKeys.UPCOMING) } returns upcomingFlow
        every { repository.observeGenres() } returns genresFlow
        every { repository.observeHomeSyncMetadata() } returns syncMetadataFlow
        coEvery { repository.refreshHome(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(repository, connectivityObserver)

    @Test
    fun `init triggers refreshHome and populates rows`() = runTest(dispatcher) {
        coEvery { repository.refreshHome(false) } coAnswers {
            trendingFlow.value = listOf(movie(1))
            popularFlow.value = listOf(movie(2))
            Result.success(Unit)
        }

        val vm = viewModel()
        vm.uiState.test {
            var state = awaitItem()
            while (state.heroMovies.isEmpty()) {
                state = awaitItem()
            }
            assertEquals(1, state.heroMovies.size)
            assertEquals(1, state.popular.size)
        }
        coVerify(exactly = 1) { repository.refreshHome(false) }
    }

    @Test
    fun `refresh sets isRefreshing during force refresh`() = runTest(dispatcher) {
        coEvery { repository.refreshHome(true) } coAnswers {
            delay(1)
            Result.success(Unit)
        }
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            skipItems(1)
            vm.refresh()

            val refreshing = awaitItem()
            assertTrue(refreshing.isRefreshing)

            dispatcher.scheduler.advanceUntilIdle()

            var state = awaitItem()
            while (state.isRefreshing) {
                state = awaitItem()
            }
            assertFalse(state.isRefreshing)
        }
        coVerify(exactly = 1) { repository.refreshHome(true) }
    }

    @Test
    fun `refresh no-ops while already refreshing`() = runTest(dispatcher) {
        coEvery { repository.refreshHome(true) } coAnswers {
            delay(1)
            Result.success(Unit)
        }
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.refresh()
        // Let the first refresh's coroutine start and flip isRefreshing to true before the
        // second call, so the guard in refresh() actually has something to check against.
        dispatcher.scheduler.runCurrent()
        vm.refresh()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.refreshHome(true) }
    }

    @Test
    fun `offline state reflects connectivity observer`() = runTest(dispatcher) {
        onlineFlow.value = false
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1) // the eager stateIn initialValue, before combine picks up onlineFlow
            dispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state.isOffline)
        }
    }

    @Test
    fun `stale sync metadata is surfaced in state`() = runTest(dispatcher) {
        syncMetadataFlow.value = SyncMetadata(123L, true, "stale")
        val vm = viewModel()
        vm.uiState.test {
            skipItems(1) // the eager stateIn initialValue, before combine picks up syncMetadataFlow
            dispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state.isStale)
            assertEquals(123L, state.lastSyncedAtEpochMillis)
        }
    }
}

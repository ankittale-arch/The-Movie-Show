package com.ankitt.themovieshow.feature.moviedetail

import app.cash.turbine.test
import com.ankitt.themovieshow.core.common.network.ConnectivityObserver
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.model.CastMember
import com.ankitt.themovieshow.core.data.model.Genre
import com.ankitt.themovieshow.core.data.model.MovieDetail
import com.ankitt.themovieshow.core.data.model.SyncMetadata
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()
    private val connectivityObserver = mockk<ConnectivityObserver>()
    private val onlineFlow = MutableStateFlow(true)

    private fun detail(id: Int) = MovieDetail(
        id = id,
        title = "Movie $id",
        overview = "overview",
        posterPath = null,
        backdropPath = null,
        releaseDate = "2024-01-01",
        runtime = 120,
        voteAverage = 7.5,
        voteCount = 100,
        tagline = "tagline",
        originalLanguage = "en",
        genres = listOf(Genre(1, "Action")),
        cast = listOf(CastMember(1, "Actor", "Character", null)),
        trailerYoutubeKey = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { connectivityObserver.isOnline } returns onlineFlow
        every { repository.isFavorite(any()) } returns MutableStateFlow(false)
        every { repository.isInWatchlist(any()) } returns MutableStateFlow(false)
        every { repository.observeMovieDetailSyncMetadata(any()) } returns
            MutableStateFlow(SyncMetadata(null, false, null))
        every { repository.observeMovieDetail(any()) } returns MutableStateFlow(null)
        coEvery { repository.recordMovieViewed(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = MovieDetailViewModel(repository, connectivityObserver)

    @Test
    fun `initial state is empty`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            val state = awaitItem()
            assertNull(state.movie)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `load success populates movie detail`() = runTest(dispatcher) {
        val detailFlow = MutableStateFlow<MovieDetail?>(null)
        every { repository.observeMovieDetail(42) } returns detailFlow
        coEvery { repository.refreshMovieDetail(42) } coAnswers {
            detailFlow.value = detail(42)
            Result.success(Unit)
        }

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.load(42)
            dispatcher.scheduler.advanceUntilIdle()

            var state = awaitItem()
            while (state.movie == null) {
                state = awaitItem()
            }
            assertEquals("Movie 42", state.movie!!.title)
            assertFalse(state.isLoading)
        }
        coVerify(exactly = 1) { repository.refreshMovieDetail(42) }
        coVerify(exactly = 1) { repository.recordMovieViewed(42) }
    }

    @Test
    fun `load failure sets error and does not wipe existing detail`() = runTest(dispatcher) {
        val detailFlow = MutableStateFlow(detail(42))
        every { repository.observeMovieDetail(42) } returns detailFlow
        coEvery { repository.refreshMovieDetail(42) } returns Result.failure(RuntimeException("boom"))

        val vm = viewModel()
        // Simulates a movie whose detail is already cached (observeMovieDetail already has data)
        // but whose refresh call fails on this load, e.g. a background re-fetch attempt.
        vm.load(42)
        dispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            skipItems(1) // the eager stateIn initialValue, before combine recomputes
            val state = awaitItem()
            assertEquals("boom", state.errorMessage)
            // existing detail from the observed flow is preserved, not wiped by the failure
            assertEquals("Movie 42", state.movie!!.title)
        }
    }

    @Test
    fun `load is idempotent per movie id`() = runTest(dispatcher) {
        every { repository.observeMovieDetail(42) } returns MutableStateFlow(detail(42))
        coEvery { repository.refreshMovieDetail(42) } returns Result.success(Unit)

        val vm = viewModel()
        vm.load(42)
        dispatcher.scheduler.advanceUntilIdle()
        vm.load(42)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.refreshMovieDetail(42) }
        coVerify(exactly = 1) { repository.recordMovieViewed(42) }
    }

    @Test
    fun `toggleFavorite delegates to repository for loaded movie`() = runTest(dispatcher) {
        every { repository.observeMovieDetail(42) } returns MutableStateFlow(detail(42))
        coEvery { repository.refreshMovieDetail(42) } returns Result.success(Unit)
        coEvery { repository.toggleFavorite(42) } returns Unit

        val vm = viewModel()
        vm.load(42)
        dispatcher.scheduler.advanceUntilIdle()
        vm.toggleFavorite()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.toggleFavorite(42) }
    }

    @Test
    fun `toggleFavorite no-ops before load`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.toggleFavorite()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { repository.toggleFavorite(any()) }
    }

    @Test
    fun `toggleWatchlist delegates to repository for loaded movie`() = runTest(dispatcher) {
        every { repository.observeMovieDetail(42) } returns MutableStateFlow(detail(42))
        coEvery { repository.refreshMovieDetail(42) } returns Result.success(Unit)
        coEvery { repository.toggleWatchlist(42) } returns Unit

        val vm = viewModel()
        vm.load(42)
        dispatcher.scheduler.advanceUntilIdle()
        vm.toggleWatchlist()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.toggleWatchlist(42) }
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
}

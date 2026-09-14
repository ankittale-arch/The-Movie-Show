package com.ankitt.themovieshow.feature.moviedetail

import app.cash.turbine.test
import com.ankitt.themovieshow.core.data.MovieRepository
import com.ankitt.themovieshow.core.data.model.KnownForMovie
import com.ankitt.themovieshow.core.data.model.PersonDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<MovieRepository>()

    private fun person(id: Int) = PersonDetail(
        id = id,
        name = "Person $id",
        biography = "bio",
        birthday = null,
        deathday = null,
        placeOfBirth = "Nowhere",
        profilePath = null,
        knownForDepartment = "Acting",
        knownFor = listOf(KnownForMovie(1, "Movie 1", null, "2024-05-01")),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PersonDetailViewModel(repository)

    @Test
    fun `initial state is empty`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.uiState.test {
            val state = awaitItem()
            assertNull(state.person)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
        }
    }

    @Test
    fun `load success populates person`() = runTest(dispatcher) {
        coEvery { repository.getPersonDetail(7) } returns Result.success(person(7))

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.load(7)

            val loading = awaitItem()
            assertEquals(true, loading.isLoading)

            dispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertEquals("Person 7", loaded.person?.name)
            assertFalse(loaded.isLoading)
            assertEquals("Acting · Nowhere", loaded.person?.meta)
            assertEquals("2024", loaded.person?.knownFor?.first()?.year)
        }
    }

    @Test
    fun `load failure sets errorMessage`() = runTest(dispatcher) {
        coEvery { repository.getPersonDetail(7) } returns Result.failure(RuntimeException("boom"))

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.load(7)
            skipItems(1) // loading = true
            dispatcher.scheduler.advanceUntilIdle()

            val failed = awaitItem()
            assertEquals("boom", failed.errorMessage)
            assertNull(failed.person)
            assertFalse(failed.isLoading)
        }
    }

    @Test
    fun `load is idempotent per person id`() = runTest(dispatcher) {
        coEvery { repository.getPersonDetail(7) } returns Result.success(person(7))

        val vm = viewModel()
        vm.load(7)
        dispatcher.scheduler.advanceUntilIdle()
        vm.load(7)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.getPersonDetail(7) }
    }

    @Test
    fun `empty biography falls back to placeholder text`() = runTest(dispatcher) {
        coEvery { repository.getPersonDetail(7) } returns Result.success(person(7).copy(biography = "   "))

        val vm = viewModel()
        vm.uiState.test {
            skipItems(1)
            vm.load(7)
            dispatcher.scheduler.advanceUntilIdle()

            var state = awaitItem()
            while (state.person == null) {
                state = awaitItem()
            }
            assertEquals("No biography available.", state.person?.biography)
        }
    }
}

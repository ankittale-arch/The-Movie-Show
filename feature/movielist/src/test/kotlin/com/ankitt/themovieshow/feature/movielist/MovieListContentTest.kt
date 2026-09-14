package com.ankitt.themovieshow.feature.movielist

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1080dp-h2400dp")
class MovieListContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: MovieListUiState,
        title: String = "Action",
        onBackClick: () -> Unit = {},
        onLoadMore: () -> Unit = {},
        onMovieClick: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TheMovieShowTheme {
                AnimatedContent(targetState = Unit, label = "test") {
                    CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                        MovieListContent(
                            title = title,
                            uiState = uiState,
                            onBackClick = onBackClick,
                            onLoadMore = onLoadMore,
                            onMovieClick = onMovieClick,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun loadingState_showsProgressIndicator() {
        setContent(uiState = MovieListUiState(isLoading = true))

        composeTestRule.onNodeWithTag("movieListLoadingIndicator").assertExists()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        setContent(uiState = MovieListUiState(isLoading = false, movies = emptyList()), title = "Sci-Fi")

        composeTestRule.onNodeWithText("No movies found for \"Sci-Fi\"").assertExists()
    }

    @Test
    fun errorState_showsErrorMessage() {
        setContent(
            uiState = MovieListUiState(isLoading = false, errorMessage = "Something went wrong", movies = emptyList()),
        )

        composeTestRule.onNodeWithText("Something went wrong").assertExists()
    }

    @Test
    fun populatedState_showsAllMovieTitles() {
        val movies = listOf(
            MovieListItem(id = 1, title = "Movie One", posterUrl = null, voteAverage = 7.5),
            MovieListItem(id = 2, title = "Movie Two", posterUrl = null, voteAverage = 8.1),
            MovieListItem(id = 3, title = "Movie Three", posterUrl = null, voteAverage = 0.0),
        )
        setContent(uiState = MovieListUiState(movies = movies))

        composeTestRule.onNodeWithText("Movie One").assertExists()
        composeTestRule.onNodeWithText("Movie Two").assertExists()
        composeTestRule.onNodeWithText("Movie Three").assertExists()
    }

    @Test
    fun clickingMovie_invokesOnMovieClickWithCorrectId() {
        val movies = listOf(
            MovieListItem(id = 1, title = "Movie One", posterUrl = null, voteAverage = 7.5),
            MovieListItem(id = 2, title = "Movie Two", posterUrl = null, voteAverage = 8.1),
        )
        var clickedId: Int? = null
        setContent(uiState = MovieListUiState(movies = movies), onMovieClick = { clickedId = it })

        composeTestRule.onNodeWithContentDescription("Movie Two").performClick()

        assert(clickedId == 2) { "expected clickedId to be 2 but was $clickedId" }
    }

    @Test
    fun loadingMoreIndicator_doesNotCrashWithPopulatedList() {
        val movies = listOf(
            MovieListItem(id = 1, title = "Movie One", posterUrl = null, voteAverage = 7.5),
        )
        setContent(uiState = MovieListUiState(movies = movies, isLoadingMore = true, hasMorePages = true))

        composeTestRule.onAllNodesWithText("Movie One").assertCountEquals(1)
    }
}

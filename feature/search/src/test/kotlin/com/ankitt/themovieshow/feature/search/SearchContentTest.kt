package com.ankitt.themovieshow.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
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
class SearchContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: SearchUiState,
        onQueryChanged: (String) -> Unit = {},
        onBackClick: () -> Unit = {},
        onMovieClick: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TheMovieShowTheme {
                AnimatedContent(targetState = Unit, label = "test") {
                    CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                        SearchContent(
                            uiState = uiState,
                            onQueryChanged = onQueryChanged,
                            onBackClick = onBackClick,
                            onMovieClick = onMovieClick,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun blankQuery_showsPrompt() {
        setContent(uiState = SearchUiState())

        composeTestRule.onNodeWithText("Search for a movie").assertExists()
    }

    @Test
    fun searching_showsProgressIndicator() {
        setContent(uiState = SearchUiState(query = "batman", isSearching = true))

        composeTestRule.onNodeWithTag("searchLoadingIndicator").assertExists()
    }

    @Test
    fun errorState_showsErrorMessage() {
        setContent(uiState = SearchUiState(query = "batman", errorMessage = "Network error"))

        composeTestRule.onNodeWithText("Network error").assertExists()
    }

    @Test
    fun noResults_showsEmptyMessage() {
        setContent(uiState = SearchUiState(query = "zzzz", results = emptyList()))

        composeTestRule.onNodeWithText("No results for \"zzzz\"").assertExists()
    }

    @Test
    fun populatedResults_showsAllTitles() {
        val results = listOf(
            SearchResultMovie(id = 1, title = "Result One", posterUrl = null, voteAverage = 7.0),
            SearchResultMovie(id = 2, title = "Result Two", posterUrl = null, voteAverage = 8.0),
        )
        setContent(uiState = SearchUiState(query = "res", results = results))

        composeTestRule.onNodeWithText("Result One").assertExists()
        composeTestRule.onNodeWithText("Result Two").assertExists()
    }

    @Test
    fun clickingResult_invokesOnMovieClickWithCorrectId() {
        val results = listOf(
            SearchResultMovie(id = 1, title = "Result One", posterUrl = null, voteAverage = 7.0),
            SearchResultMovie(id = 2, title = "Result Two", posterUrl = null, voteAverage = 8.0),
        )
        var clickedId: Int? = null
        setContent(uiState = SearchUiState(query = "res", results = results), onMovieClick = { clickedId = it })

        composeTestRule.onNodeWithContentDescription("Result Two").performClick()

        assert(clickedId == 2) { "expected clickedId to be 2 but was $clickedId" }
    }
}

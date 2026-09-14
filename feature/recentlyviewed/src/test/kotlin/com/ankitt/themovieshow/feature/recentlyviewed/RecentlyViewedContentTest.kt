package com.ankitt.themovieshow.feature.recentlyviewed

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ankitt.themovieshow.core.designsystem.theme.TheMovieShowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1080dp-h2400dp")
class RecentlyViewedContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: RecentlyViewedUiState,
        onBackClick: () -> Unit = {},
        onMovieClick: (Int) -> Unit = {},
        onClearHistoryClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TheMovieShowTheme {
                RecentlyViewedContent(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onMovieClick = onMovieClick,
                    onClearHistoryClick = onClearHistoryClick,
                )
            }
        }
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        setContent(uiState = RecentlyViewedUiState())

        composeTestRule.onNodeWithText("No movies viewed yet").assertExists()
    }

    @Test
    fun emptyState_hidesClearHistoryButton() {
        setContent(uiState = RecentlyViewedUiState())

        composeTestRule.onNodeWithContentDescription("Clear history").assertDoesNotExist()
    }

    @Test
    fun populatedState_showsAllMovieTitles() {
        val movies = listOf(
            RecentlyViewedItem(id = 1, title = "Recent One", posterUrl = null, voteAverage = 7.0),
            RecentlyViewedItem(id = 2, title = "Recent Two", posterUrl = null, voteAverage = 8.0),
        )
        setContent(uiState = RecentlyViewedUiState(movies = movies))

        composeTestRule.onNodeWithText("Recent One").assertExists()
        composeTestRule.onNodeWithText("Recent Two").assertExists()
    }

    @Test
    fun clickingMovie_invokesOnMovieClickWithCorrectId() {
        val movies = listOf(
            RecentlyViewedItem(id = 1, title = "Recent One", posterUrl = null, voteAverage = 7.0),
            RecentlyViewedItem(id = 2, title = "Recent Two", posterUrl = null, voteAverage = 8.0),
        )
        var clickedId: Int? = null
        setContent(uiState = RecentlyViewedUiState(movies = movies), onMovieClick = { clickedId = it })

        composeTestRule.onNodeWithContentDescription("Recent Two").performClick()

        assert(clickedId == 2) { "expected clickedId to be 2 but was $clickedId" }
    }

    @Test
    fun clickingClearHistory_invokesCallback() {
        val movies = listOf(
            RecentlyViewedItem(id = 1, title = "Recent One", posterUrl = null, voteAverage = 7.0),
        )
        var cleared = false
        setContent(uiState = RecentlyViewedUiState(movies = movies), onClearHistoryClick = { cleared = true })

        composeTestRule.onNodeWithContentDescription("Clear history").performClick()

        assert(cleared)
    }
}

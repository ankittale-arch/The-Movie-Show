package com.ankitt.themovieshow.feature.bookmarks

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
class BookmarksContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: BookmarksUiState,
        onBackClick: () -> Unit = {},
        onMovieClick: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TheMovieShowTheme {
                BookmarksContent(uiState = uiState, onBackClick = onBackClick, onMovieClick = onMovieClick)
            }
        }
    }

    @Test
    fun emptyFavoritesTab_showsEmptyMessage() {
        setContent(uiState = BookmarksUiState())

        composeTestRule.onNodeWithText("No favorites yet").assertExists()
    }

    @Test
    fun emptyWatchlistTab_showsEmptyMessage() {
        setContent(uiState = BookmarksUiState())

        composeTestRule.onNodeWithText("Watchlist").performClick()

        composeTestRule.onNodeWithText("Nothing in your watchlist yet").assertExists()
    }

    @Test
    fun populatedFavorites_showsItems() {
        val favorites = listOf(
            BookmarkItem(id = 1, title = "Favorite One", posterUrl = null, voteAverage = 7.5),
            BookmarkItem(id = 2, title = "Favorite Two", posterUrl = null, voteAverage = 8.1),
        )
        setContent(uiState = BookmarksUiState(favorites = favorites))

        composeTestRule.onNodeWithText("Favorite One").assertExists()
        composeTestRule.onNodeWithText("Favorite Two").assertExists()
    }

    @Test
    fun populatedWatchlist_showsItems() {
        val watchlist = listOf(
            BookmarkItem(id = 3, title = "Watch One", posterUrl = null, voteAverage = 6.0),
        )
        setContent(uiState = BookmarksUiState(watchlist = watchlist))

        composeTestRule.onNodeWithText("Watchlist").performClick()

        composeTestRule.onNodeWithText("Watch One").assertExists()
    }

    @Test
    fun clickingBookmark_invokesOnMovieClickWithCorrectId() {
        val favorites = listOf(
            BookmarkItem(id = 1, title = "Favorite One", posterUrl = null, voteAverage = 7.5),
            BookmarkItem(id = 2, title = "Favorite Two", posterUrl = null, voteAverage = 8.1),
        )
        var clickedId: Int? = null
        setContent(uiState = BookmarksUiState(favorites = favorites), onMovieClick = { clickedId = it })

        composeTestRule.onNodeWithContentDescription("Favorite Two").performClick()

        assert(clickedId == 2) { "expected clickedId to be 2 but was $clickedId" }
    }
}

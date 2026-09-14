package com.ankitt.themovieshow.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
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
class HomeContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleState() = HomeUiState(
        heroMovies = listOf(
            HomeMovie(id = 1, title = "Hero Movie", posterUrl = null, backdropUrl = null, voteAverage = 8.2),
        ),
        genres = listOf(
            HomeGenre(id = 10, name = "Action"),
            HomeGenre(id = 11, name = "Comedy"),
        ),
        nowPlaying = listOf(
            HomeMovie(id = 2, title = "Now Playing One", posterUrl = null, backdropUrl = null, voteAverage = 7.0),
        ),
        popular = listOf(
            HomeMovie(id = 3, title = "Popular One", posterUrl = null, backdropUrl = null, voteAverage = 7.5),
        ),
        discover = listOf(
            HomeMovie(id = 4, title = "Discover One", posterUrl = null, backdropUrl = null, voteAverage = 6.5),
        ),
        upcoming = listOf(
            HomeMovie(id = 5, title = "Upcoming One", posterUrl = null, backdropUrl = null, voteAverage = 6.0),
        ),
    )

    private fun setContent(
        uiState: HomeUiState,
        onSearchClick: () -> Unit = {},
        onBookmarksClick: () -> Unit = {},
        onRecentlyViewedClick: () -> Unit = {},
        onMovieListClick: (String, String) -> Unit = { _, _ -> },
        onMovieClick: (Int) -> Unit = {},
        onRefresh: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TheMovieShowTheme {
                AnimatedContent(targetState = Unit, label = "test") {
                    CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                        HomeContent(
                            uiState = uiState,
                            onSearchClick = onSearchClick,
                            onBookmarksClick = onBookmarksClick,
                            onRecentlyViewedClick = onRecentlyViewedClick,
                            onMovieListClick = onMovieListClick,
                            onMovieClick = onMovieClick,
                            onRefresh = onRefresh,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun populatedState_showsSectionTitlesAndMovies() {
        setContent(uiState = sampleState())

        composeTestRule.onNodeWithText("Now Playing").assertExists()
        composeTestRule.onNodeWithText("Popular Movies").assertExists()
        composeTestRule.onNodeWithText("Discover Movies").assertExists()
        composeTestRule.onNodeWithText("Upcoming Movies").assertExists()
        composeTestRule.onNodeWithContentDescription("Now Playing One").assertExists()
        composeTestRule.onNodeWithText("Action").assertExists()
    }

    @Test
    fun emptyState_rendersWithoutCrashing() {
        setContent(uiState = HomeUiState())

        composeTestRule.onNodeWithText("Now Playing").assertExists()
    }

    @Test
    fun clickingMovieCard_invokesOnMovieClickWithCorrectId() {
        var clickedId: Int? = null
        setContent(uiState = sampleState(), onMovieClick = { clickedId = it })

        composeTestRule.onNodeWithContentDescription("Now Playing One").performClick()

        assert(clickedId == 2) { "expected clickedId to be 2 but was $clickedId" }
    }

    @Test
    fun clickingMoreLink_invokesOnMovieListClick() {
        var clickedKey: String? = null
        var clickedTitle: String? = null
        setContent(
            uiState = sampleState(),
            onMovieListClick = { key, title -> clickedKey = key; clickedTitle = title },
        )

        composeTestRule.onAllNodesWithText("more").onFirst().performClick()

        assert(clickedTitle == "Now Playing") { "expected 'Now Playing' but was $clickedTitle" }
        assert(clickedKey != null)
    }

    @Test
    fun clickingGenreChip_invokesOnMovieListClick() {
        var clickedTitle: String? = null
        setContent(
            uiState = sampleState(),
            onMovieListClick = { _, title -> clickedTitle = title },
        )

        composeTestRule.onNodeWithText("Comedy").performClick()

        assert(clickedTitle == "Comedy") { "expected 'Comedy' but was $clickedTitle" }
    }

    @Test
    fun refreshingState_rendersWithoutCrashing() {
        setContent(uiState = sampleState().copy(isRefreshing = true))

        composeTestRule.onNodeWithText("Now Playing").assertExists()
    }
}

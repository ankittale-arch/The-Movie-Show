package com.ankitt.themovieshow.feature.moviedetail

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
class MovieDetailContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleMovie(
        cast: List<CastMemberUi> = emptyList(),
        genres: List<String> = listOf("Action", "Sci-Fi"),
    ) = MovieDetailUi(
        id = 1,
        title = "Sample Movie",
        overview = "A sample overview.",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2024-01-01",
        durationText = "2h 10m",
        rating = 7.8,
        language = "English",
        tagline = "A sample tagline",
        genres = genres,
        cast = cast,
        trailerYoutubeKey = null,
    )

    private fun setContent(
        uiState: MovieDetailUiState,
        onBackClick: () -> Unit = {},
        onFavoriteClick: () -> Unit = {},
        onWatchlistClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            TheMovieShowTheme {
                AnimatedContent(targetState = Unit, label = "test") {
                    CompositionLocalProvider(LocalNavAnimatedContentScope provides this@AnimatedContent) {
                        MovieDetailContent(
                            uiState = uiState,
                            onBackClick = onBackClick,
                            onFavoriteClick = onFavoriteClick,
                            onWatchlistClick = onWatchlistClick,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun loadingState_showsProgressIndicator() {
        setContent(uiState = MovieDetailUiState(isLoading = true))

        composeTestRule.onNodeWithTag("movieDetailLoadingIndicator").assertExists()
    }

    @Test
    fun errorState_showsErrorMessage() {
        setContent(uiState = MovieDetailUiState(errorMessage = "Failed to load movie"))

        composeTestRule.onNodeWithText("Failed to load movie").assertExists()
    }

    @Test
    fun noMovieNoError_showsNotFoundMessage() {
        setContent(uiState = MovieDetailUiState())

        composeTestRule.onNodeWithText("Movie not found").assertExists()
    }

    @Test
    fun populatedState_showsMovieDetails() {
        val movie = sampleMovie(
            cast = listOf(
                CastMemberUi(id = 1, name = "Actor One", character = "Hero", profileUrl = null),
                CastMemberUi(id = 2, name = "Actor Two", character = "Villain", profileUrl = null),
            ),
        )
        setContent(uiState = MovieDetailUiState(movie = movie))

        composeTestRule.onNodeWithText("Sample Movie").assertExists()
        composeTestRule.onNodeWithText("A sample tagline").assertExists()
        composeTestRule.onNodeWithText("Actor One").assertExists()
        composeTestRule.onNodeWithText("Actor Two").assertExists()
    }

    @Test
    fun clickingFavorite_invokesCallback() {
        var favoriteClicked = false
        setContent(
            uiState = MovieDetailUiState(movie = sampleMovie()),
            onFavoriteClick = { favoriteClicked = true },
        )

        composeTestRule.onNodeWithContentDescription("Favorite").performClick()

        assert(favoriteClicked)
    }

    @Test
    fun clickingWatchlist_invokesCallback() {
        var watchlistClicked = false
        setContent(
            uiState = MovieDetailUiState(movie = sampleMovie()),
            onWatchlistClick = { watchlistClicked = true },
        )

        composeTestRule.onNodeWithContentDescription("Watchlist").performClick()

        assert(watchlistClicked)
    }

    @Test
    fun clickingBack_invokesCallback() {
        var backClicked = false
        setContent(uiState = MovieDetailUiState(), onBackClick = { backClicked = true })

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assert(backClicked)
    }
}

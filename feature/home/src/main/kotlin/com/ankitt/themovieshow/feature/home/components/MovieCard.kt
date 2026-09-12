package com.ankitt.themovieshow.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.animation.sharedMovieElement
import com.ankitt.themovieshow.core.designsystem.components.RatingBadge
import com.ankitt.themovieshow.core.designsystem.text.clipToWords

/** The three ways a movie poster/backdrop and its title are laid out across Home's rows. */
enum class MovieCardVariant {
    /** Portrait poster, no title (Now Playing, Popular Movies). */
    PosterOnly,

    /** Portrait poster with the title as a caption below (Upcoming Movies). */
    PosterWithCaption,

    /** Wide landscape image with the title overlaid at the bottom (Discover Movies). */
    LandscapeOverlay,
}

@Composable
fun MovieCard(
    movieId: Int,
    title: String,
    imageUrl: String?,
    variant: MovieCardVariant,
    modifier: Modifier = Modifier,
    rating: Double = 0.0,
    onClick: () -> Unit = {},
) {
    val cardWidth = if (variant == MovieCardVariant.LandscapeOverlay) 220.dp else 120.dp
    val aspectRatio = if (variant == MovieCardVariant.LandscapeOverlay) 16f / 9f else 2f / 3f

    Column(modifier = modifier.width(cardWidth)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .let {
                        // Only the poster variants share the detail screen's poster image —
                        // Discover's LandscapeOverlay shows a backdrop, a different image, so it
                        // keeps the plain default screen transition instead of morphing into a
                        // mismatched shape.
                        if (variant == MovieCardVariant.LandscapeOverlay) {
                            it
                        } else {
                            it.sharedMovieElement(
                                key = "movie-poster-$movieId",
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            )
                        }
                    },
            )
            // Discover Movies (LandscapeOverlay) deliberately excluded — its title overlay
            // already occupies the card, and it's the one row this badge should not appear on.
            if (variant != MovieCardVariant.LandscapeOverlay && rating > 0.0) {
                RatingBadge(
                    rating = rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
            if (variant == MovieCardVariant.LandscapeOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            ),
                        ),
                )
                Text(
                    text = title.clipToWords(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                )
            }
        }
        if (variant == MovieCardVariant.PosterWithCaption) {
            Text(
                text = title.clipToWords(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

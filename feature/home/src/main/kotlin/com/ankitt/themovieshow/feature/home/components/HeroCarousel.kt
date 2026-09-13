package com.ankitt.themovieshow.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.ankitt.themovieshow.core.designsystem.components.RatingBadge
import com.ankitt.themovieshow.core.designsystem.text.clipToWords
import com.ankitt.themovieshow.feature.home.HomeMovie
import kotlin.math.absoluteValue

private val CardWidth = 312.dp
private val CardOverlap = 48.dp
private val CardCornerRadius = 16.dp

@Composable
fun HeroCarousel(
    movies: List<HomeMovie>,
    modifier: Modifier = Modifier,
    onMovieClick: (Int) -> Unit = {},
) {
    if (movies.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { movies.size })

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val sidePadding = ((maxWidth - CardWidth) / 2).coerceAtLeast(20.dp)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = sidePadding),
            pageSpacing = -CardOverlap,
            pageSize = PageSize.Fixed(CardWidth),
        ) { page ->
            val movie = movies[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .zIndex(1f - focusDistance(pagerState, page))
                    .graphicsLayer {
                        // 0 at the focused/centered page, growing toward 1 for neighbors —
                        // drives the "focused card upfront, others tucked behind it" stack.
                        val distance = focusDistance(pagerState, page)
                        val focus = 1f - distance
                        val scale = lerp(0.82f, 1f, focus)
                        scaleX = scale
                        scaleY = scale
                        translationY = lerp(14.dp.toPx(), 0f, focus)
                        alpha = lerp(0.55f, 1f, focus)
                        shadowElevation = lerp(6f, 28f, focus)
                        shape = RoundedCornerShape(CardCornerRadius)
                        clip = true
                    }
                    .clickable { onMovieClick(movie.id) },
            ) {
                AsyncImage(
                    model = movie.backdropUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            ),
                        ),
                )
                Text(
                    text = movie.title.clipToWords(),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                )
                if (movie.voteAverage > 0.0) {
                    RatingBadge(
                        rating = movie.voteAverage,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}

/** 0 when [page] is the centered/focused page, growing to 1 as it scrolls away — the single
 * distance value that drives scale, lift, dimming, elevation and stacking order together. */
private fun focusDistance(pagerState: androidx.compose.foundation.pager.PagerState, page: Int): Float =
    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)

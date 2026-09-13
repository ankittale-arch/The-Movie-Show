package com.ankitt.themovieshow.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ankitt.themovieshow.feature.home.HomeMovie

@Composable
fun MovieSection(
    title: String,
    movies: List<HomeMovie>,
    variant: MovieCardVariant,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onMoreClick: () -> Unit = {},
    onMovieClick: (Int) -> Unit = {},
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                leadingIcon?.invoke()
            }
            Row(
                modifier = Modifier.clickable(onClick = onMoreClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "more",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                val imageUrl = if (variant == MovieCardVariant.LandscapeOverlay) {
                    movie.backdropUrl
                } else {
                    movie.posterUrl
                }
                MovieCard(
                    movieId = movie.id,
                    title = movie.title,
                    imageUrl = imageUrl,
                    variant = variant,
                    rating = movie.voteAverage,
                    onClick = { onMovieClick(movie.id) },
                )
            }
        }
    }
}

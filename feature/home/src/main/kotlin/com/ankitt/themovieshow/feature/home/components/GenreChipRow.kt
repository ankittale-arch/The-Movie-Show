package com.ankitt.themovieshow.feature.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ankitt.themovieshow.core.designsystem.components.Chip
import com.ankitt.themovieshow.feature.home.HomeGenre

@Composable
fun GenreChipRow(
    genres: List<HomeGenre>,
    modifier: Modifier = Modifier,
    onGenreClick: (HomeGenre) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(genres, key = { it.id }) { genre ->
            Chip(text = genre.name, onClick = { onGenreClick(genre) })
        }
    }
}

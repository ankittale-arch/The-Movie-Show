package com.ankitt.themovieshow.feature.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onSearchClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onRecentlyViewedClick: () -> Unit = {},
) {
    TopAppBar(
        title = { Text("The Movie Show") },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = onRecentlyViewedClick) {
                Icon(imageVector = Icons.Filled.History, contentDescription = "Recently Viewed")
            }
            IconButton(onClick = onBookmarksClick) {
                Icon(imageVector = Icons.Filled.Bookmark, contentDescription = "Bookmarks")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}

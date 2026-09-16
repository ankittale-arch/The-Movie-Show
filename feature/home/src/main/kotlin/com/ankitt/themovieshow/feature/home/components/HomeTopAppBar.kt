package com.ankitt.themovieshow.feature.home.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar() {
    TopAppBar(
        title = { Text("The Movie Show") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}

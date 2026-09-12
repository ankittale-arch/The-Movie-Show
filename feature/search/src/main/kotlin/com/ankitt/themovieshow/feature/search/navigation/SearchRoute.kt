package com.ankitt.themovieshow.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ankitt.themovieshow.feature.search.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

fun EntryProviderScope<NavKey>.searchEntry(onBackClick: () -> Unit) {
    entry<SearchRoute> { SearchScreen(onBackClick = onBackClick) }
}

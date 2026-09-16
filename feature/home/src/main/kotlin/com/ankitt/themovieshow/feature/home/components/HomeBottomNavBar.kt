package com.ankitt.themovieshow.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Which tab of the floating [HomeBottomNavBar] is currently highlighted. */
enum class HomeBottomNavTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    Home(label = "Home", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home),
    Search(label = "Search", selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search),
    Bookmarks(
        label = "Bookmarks",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
    ),
    Recent(label = "Recent", selectedIcon = Icons.Filled.History, unselectedIcon = Icons.Outlined.History),
}

/**
 * The floating, pill-shaped bottom navigation bar shown on Home — a rounded [Surface] with an
 * evenly spaced row of tabs, the selected one highlighted with its own smaller pill.
 */
@Composable
fun HomeBottomNavBar(
    selectedTab: HomeBottomNavTab,
    onTabSelected: (HomeBottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeBottomNavTab.entries.forEach { tab ->
                HomeBottomNavItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HomeBottomNavItem(
    tab: HomeBottomNavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

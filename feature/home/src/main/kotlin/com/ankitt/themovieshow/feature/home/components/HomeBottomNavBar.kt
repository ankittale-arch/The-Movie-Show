package com.ankitt.themovieshow.feature.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
 *
 * When [expanded] is false (the user is scrolling down) the bar compresses into a compact pill
 * showing only the selected tab: the other tabs shrink and fade out while the surface animates
 * down to its new width, and they slide back in when [expanded] flips to true again.
 */
@Composable
fun HomeBottomNavBar(
    selectedTab: HomeBottomNavTab,
    onTabSelected: (HomeBottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
    ) {
        Row(
            modifier = Modifier
                .then(if (expanded) Modifier.fillMaxWidth() else Modifier)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeBottomNavTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                AnimatedVisibility(
                    visible = expanded || selected,
                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.CenterHorizontally),
                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally),
                ) {
                    HomeBottomNavItem(
                        tab = tab,
                        selected = selected,
                        onClick = { onTabSelected(tab) },
                    )
                }
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
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "navItemContentColor",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        label = "navItemContainerColor",
    )
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = containerColor,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
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

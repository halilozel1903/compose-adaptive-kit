package io.github.halilozel1903.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.core.NavigationType

/**
 * A destination of [AdaptiveNavigation].
 *
 * @property key identifies the destination; [AdaptiveNavigation] reports it to `onSelect`.
 * @property badgeCount shown as a badge when greater than 0.
 */
@Immutable
public data class AdaptiveNavItem(
    public val key: String,
    public val label: String,
    public val icon: ImageVector,
    public val badgeCount: Int = 0,
)

/**
 * Top level navigation that follows the window: a bottom bar on compact widths, a navigation rail on medium widths
 * and short windows, and a permanent navigation drawer on expanded widths and larger.
 *
 * ```
 * AdaptiveNavigation(
 *     items = listOf(AdaptiveNavItem("inbox", "Inbox", InboxIcon, badgeCount = 4), ...),
 *     selectedKey = section,
 *     onSelect = { section = it },
 * ) {
 *     when (section) { ... }
 * }
 * ```
 *
 * @param navigationType which navigation to show. Defaults to the recommendation for the current window.
 * @param header shown at the top of the rail and the drawer (an app name, a compose button), not in the bottom bar.
 * @param drawerWidth the width of the permanent drawer.
 */
@Composable
public fun AdaptiveNavigation(
    items: List<AdaptiveNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    navigationType: NavigationType = rememberWindowLayoutInfo().navigationType,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    drawerWidth: Dp = 240.dp,
    content: @Composable () -> Unit,
) {
    when (navigationType) {
        NavigationType.BottomBar -> Column(modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = item.key == selectedKey,
                        onClick = { onSelect(item.key) },
                        icon = { NavIcon(item) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
        NavigationType.Rail -> Row(modifier.fillMaxSize()) {
            NavigationRail(header = header) {
                Spacer(Modifier.height(8.dp))
                items.forEach { item ->
                    NavigationRailItem(
                        selected = item.key == selectedKey,
                        onClick = { onSelect(item.key) },
                        icon = { NavIcon(item) },
                        label = { Text(item.label) },
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) { content() }
        }
        NavigationType.Drawer -> PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(Modifier.width(drawerWidth)) {
                    if (header != null) header()
                    Spacer(Modifier.height(12.dp))
                    items.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            selected = item.key == selectedKey,
                            onClick = { onSelect(item.key) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            badge = if (item.badgeCount > 0) {
                                { Text(item.badgeCount.toString()) }
                            } else {
                                null
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize(),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavIcon(item: AdaptiveNavItem) {
    if (item.badgeCount > 0) {
        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
            Icon(item.icon, contentDescription = null)
        }
    } else {
        Icon(item.icon, contentDescription = null)
    }
}

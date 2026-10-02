package io.github.halilozel1903.adaptive.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.AdaptiveDebugOverlay
import io.github.halilozel1903.adaptive.AdaptiveNavItem
import io.github.halilozel1903.adaptive.AdaptiveNavigation
import io.github.halilozel1903.adaptive.core.NavigationType
import io.github.halilozel1903.adaptive.rememberWindowLayoutInfo

/**
 * Harbor: mail with a list-detail layout and notes with a supporting pane, inside navigation that switches
 * between a bottom bar, a rail and a drawer with the window size.
 */
@Composable
fun SampleApp(scene: Scene?, initialDebug: Boolean) {
    val windowInfo = rememberWindowLayoutInfo()
    var section by rememberSaveable { mutableStateOf(if (scene == Scene.Supporting) Sections.NOTES else Sections.INBOX) }
    var selectedEmailId by rememberSaveable { mutableStateOf<Int?>(if (scene == Scene.ListDetail) 1 else null) }
    var selectedNoteId by rememberSaveable { mutableIntStateOf(1) }
    var debug by rememberSaveable { mutableStateOf(initialDebug) }
    val navigationType = windowInfo.navigationType
    val unread = remember { SampleData.emails.count { it.unread } }
    val items = remember(unread) {
        listOf(
            AdaptiveNavItem(Sections.INBOX, "Inbox", SampleIcons.Inbox, badgeCount = unread),
            AdaptiveNavItem(Sections.STARRED, "Starred", SampleIcons.Star),
            AdaptiveNavItem(Sections.SENT, "Sent", SampleIcons.Send),
            AdaptiveNavItem(Sections.NOTES, "Notes", SampleIcons.Notes),
        )
    }
    val toggleDebug = { debug = !debug }

    Box(Modifier.fillMaxSize()) {
        AdaptiveNavigation(
            items = items,
            selectedKey = section,
            onSelect = { key ->
                section = key
                selectedEmailId = null
            },
            navigationType = navigationType,
            header = { NavigationHeader(navigationType) },
        ) {
            if (section == Sections.NOTES) {
                NotesScreen(
                    selectedNoteId = selectedNoteId,
                    onOpenNote = { selectedNoteId = it },
                    windowInfo = windowInfo,
                    onToggleDebug = toggleDebug,
                )
            } else {
                MailScreen(
                    section = section,
                    selectedId = selectedEmailId,
                    onSelect = { selectedEmailId = it },
                    onBack = { selectedEmailId = null },
                    windowInfo = windowInfo,
                    onToggleDebug = toggleDebug,
                )
            }
        }
        if (debug) {
            AdaptiveDebugOverlay(
                windowInfo = windowInfo,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    // Keep the label above the bottom bar on phones.
                    .padding(bottom = if (navigationType == NavigationType.BottomBar) 80.dp else 0.dp),
            )
        }
    }
}

@Composable
private fun ColumnScope.NavigationHeader(navigationType: NavigationType) {
    if (navigationType == NavigationType.Drawer) {
        Text(
            text = "Harbor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 28.dp, top = 20.dp, bottom = 16.dp),
        )
        ExtendedFloatingActionButton(
            onClick = {},
            icon = { Icon(SampleIcons.Edit, contentDescription = null) },
            text = { Text("New message") },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    } else {
        FloatingActionButton(onClick = {}, modifier = Modifier.padding(top = 8.dp)) {
            Icon(SampleIcons.Edit, contentDescription = "New message")
        }
    }
}

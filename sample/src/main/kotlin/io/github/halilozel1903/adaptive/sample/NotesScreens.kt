package io.github.halilozel1903.adaptive.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.LocalSupportingPanePlacement
import io.github.halilozel1903.adaptive.SupportingPaneLayout
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.SupportingArrangement
import io.github.halilozel1903.adaptive.core.SupportingPaneConfig
import io.github.halilozel1903.adaptive.core.WindowWidthClass

/** Notes: the note in the main pane, related notes and an outline in the supporting pane. */
@Composable
fun NotesScreen(
    selectedNoteId: Int,
    onOpenNote: (Int) -> Unit,
    windowInfo: AdaptiveWindowInfo,
    onToggleDebug: () -> Unit,
) {
    val note = SampleData.notes.firstOrNull { it.id == selectedNoteId } ?: SampleData.notes.first()
    val compact = windowInfo.sizeClass.widthClass == WindowWidthClass.Compact
    SupportingPaneLayout(
        main = {
            PaneCard(compact) {
                key(note.id) { NoteMain(note, onToggleDebug) }
            }
        },
        supporting = {
            SupportingCard(compact) {
                key(note.id) { NoteSupporting(note, onOpenNote) }
            }
        },
        config = SupportingPaneConfig(spacing = if (compact) 0f else 12f),
        windowInfo = windowInfo,
        modifier = Modifier
            .fillMaxSize()
            .screenPadding(compact),
    )
}

/** On phones the supporting pane sits under the note as a raised sheet. */
@Composable
private fun SupportingCard(compact: Boolean, content: @Composable () -> Unit) {
    val stacked = LocalSupportingPanePlacement.current.arrangement == SupportingArrangement.Stacked
    if (compact && stacked) {
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxSize(),
        ) { content() }
    } else {
        PaneCard(compact, content)
    }
}

@Composable
private fun NoteMain(note: Note, onToggleDebug: () -> Unit) {
    val checked = remember(note.id) { mutableStateListOf(*note.checklist.map { it.second }.toTypedArray()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding(),
    ) {
        ScreenHeader(title = "Notes", subtitle = "${SampleData.notes.size} notes", onToggleDebug = onToggleDebug)
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(text = note.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = note.edited,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            TagRow(note.tags)
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                note.paragraphs.forEach { Text(text = it, style = MaterialTheme.typography.bodyLarge) }
            }
            if (note.checklist.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("Checklist")
                Spacer(Modifier.height(4.dp))
                note.checklist.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { checked[index] = !checked[index] },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked[index], onCheckedChange = { checked[index] = it })
                        Text(text = item.first, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteSupporting(note: Note, onOpenNote: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    // Notes sharing a tag first, then the rest.
    val related = remember(note.id) {
        SampleData.notes.filter { it.id != note.id }.sortedByDescending { other -> other.tags.count { it in note.tags } }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Text(text = "Related notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            related.take(3).forEach { other ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNote(other.id) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(text = other.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = other.edited, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = other.paragraphs.first(),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        if (note.outline.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionTitle("Outline")
            Spacer(Modifier.height(8.dp))
            note.outline.forEachIndexed { index, heading ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = colors.primaryContainer, modifier = Modifier.size(24.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(text = heading, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

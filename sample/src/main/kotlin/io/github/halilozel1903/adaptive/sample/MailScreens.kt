package io.github.halilozel1903.adaptive.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.adaptive.AdaptiveListDetail
import io.github.halilozel1903.adaptive.LocalPaneLayout
import io.github.halilozel1903.adaptive.core.AdaptiveWindowInfo
import io.github.halilozel1903.adaptive.core.PaneConfig
import io.github.halilozel1903.adaptive.core.PaneMode
import io.github.halilozel1903.adaptive.core.WindowWidthClass

/** Mail: list-detail, with thread details as a third pane when there is room. */
@Composable
fun MailScreen(
    section: String,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    windowInfo: AdaptiveWindowInfo,
    onToggleDebug: () -> Unit,
) {
    val emails = remember(section) { SampleData.emailsFor(section) }
    val selected = emails.firstOrNull { it.id == selectedId }
    val compact = windowInfo.sizeClass.widthClass == WindowWidthClass.Compact
    val title = when (section) {
        Sections.STARRED -> "Starred"
        Sections.SENT -> "Sent"
        else -> "Inbox"
    }
    AdaptiveListDetail<Email>(
        list = {
            PaneCard(compact) { MailList(title, emails, selectedId, onSelect, onToggleDebug) }
        },
        detail = { email ->
            PaneCard(compact) { EmailDetail(email, onBack) }
        },
        selected = selected,
        onBack = onBack,
        extra = { email ->
            PaneCard(compact) { ThreadDetails(email) }
        },
        detailPlaceholder = {
            PaneCard(compact) { EmptyDetail() }
        },
        config = PaneConfig(spacing = if (compact) 0f else 12f),
        windowInfo = windowInfo,
        modifier = Modifier
            .fillMaxSize()
            .screenPadding(compact),
    )
}

@Composable
private fun MailList(
    title: String,
    emails: List<Email>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onToggleDebug: () -> Unit,
) {
    val showSelection = LocalPaneLayout.current.mode != PaneMode.Single
    val unread = emails.count { it.unread }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            ScreenHeader(
                title = title,
                subtitle = if (unread > 0) "$unread unread" else "${emails.size} messages",
                onToggleDebug = onToggleDebug,
                modifier = Modifier.statusBarsPadding(),
            )
        }
        items(emails, key = { it.id }) { email ->
            EmailRow(
                email = email,
                selected = showSelection && email.id == selectedId,
                onClick = { onSelect(email.id) },
            )
        }
    }
}

@Composable
private fun EmailRow(email: Email, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Avatar(email.sender)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = email.sender,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (email.unread) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = email.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (email.unread) colors.primary else colors.onSurfaceVariant,
                )
            }
            Text(
                text = email.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (email.unread) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = email.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (email.starred) {
                    Icon(
                        imageVector = SampleIcons.Star,
                        contentDescription = "Starred",
                        tint = colors.tertiary,
                        modifier = Modifier.padding(start = 6.dp).size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailDetail(email: Email, onBack: () -> Unit) {
    val mode = LocalPaneLayout.current.mode
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        if (mode == PaneMode.Single) {
            IconButton(onClick = onBack, modifier = Modifier.padding(bottom = 4.dp)) {
                Icon(SampleIcons.Back, contentDescription = "Back")
            }
        }
        Text(text = email.subject, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        TagRow(email.labels)
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(email.sender, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = email.sender, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${email.senderAddress} · to ${email.participants.filter { it != email.sender }.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(text = email.time, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        HorizontalDivider(Modifier.padding(vertical = 20.dp), color = colors.outlineVariant)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            email.body.forEach { paragraph ->
                Text(text = paragraph, style = MaterialTheme.typography.bodyLarge)
            }
        }
        // Without a third pane, the attachments go under the message.
        if (mode != PaneMode.ThreePane && email.attachments.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionTitle("Attachments")
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                email.attachments.forEach { AttachmentRow(it) }
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {}) { Text("Reply") }
            OutlinedButton(onClick = {}) { Text("Forward") }
        }
    }
}

@Composable
private fun ThreadDetails(email: Email) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Text(text = "Thread details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        SectionTitle("People")
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            email.participants.forEach { name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(name, size = 32.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(text = name, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Attachments")
        Spacer(Modifier.height(10.dp))
        if (email.attachments.isEmpty()) {
            Text(text = "No attachments", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                email.attachments.forEach { AttachmentRow(it) }
            }
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Labels")
        Spacer(Modifier.height(10.dp))
        TagRow(email.labels)
    }
}

@Composable
private fun AttachmentRow(attachment: Attachment) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(SampleIcons.Attach, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = attachment.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = attachment.size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyDetail() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = SampleIcons.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(text = "Select a message", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Messages you open appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

package dk.babyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.BabyTouch
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import kotlinx.coroutines.delay

/**
 * A running or paused timer, shown like a mini player above the menu bar. [elapsedSeconds] is read
 * again every second while [running], so the time keeps counting without anything saved.
 */
@Composable
fun TimerBar(
    title: String,
    category: CareCategory,
    running: Boolean,
    elapsedSeconds: () -> Long,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onOpen: (() -> Unit)? = null,
) {
    val currentElapsed by rememberUpdatedState(elapsedSeconds)
    var seconds by remember { mutableLongStateOf(elapsedSeconds()) }
    LaunchedEffect(running) {
        seconds = currentElapsed()
        while (running) {
            delay(1_000 - System.currentTimeMillis() % 1_000)
            seconds = currentElapsed()
        }
    }
    // A paused timer can still change, e.g. after editing, so it is read directly instead of from the ticker.
    val shown = if (running) seconds else elapsedSeconds()
    val colors = MaterialTheme.babyColors.category(category)
    Surface(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            Modifier.heightIn(min = 72.dp).padding(horizontal = BabySpacing.m, vertical = BabySpacing.s),
            horizontalArrangement = Arrangement.spacedBy(BabySpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f).then(if (onOpen != null) Modifier.clickable(role = Role.Button, onClick = onOpen) else Modifier)
                    .semantics(mergeDescendants = true) { contentDescription = listOfNotNull(title, formatTimer(shown), subtitle, if (running) null else "sat på pause").joinToString(", ") },
                horizontalArrangement = Arrangement.spacedBy(BabySpacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(40.dp).background(colors.container, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatTimer(shown), style = MaterialTheme.typography.titleLarge)
                    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            FilledTonalIconButton(onClick = onToggle, modifier = Modifier.size(BabyTouch.minimum)) {
                Icon(if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = if (running) "Pause $title" else "Fortsæt $title")
            }
            FilledIconButton(onClick = onStop, modifier = Modifier.size(BabyTouch.minimum)) {
                Icon(Icons.Outlined.Stop, contentDescription = "Stop $title")
            }
        }
    }
}

/** 4:05 for minutes and seconds, 1:04:05 once an hour has passed. */
fun formatTimer(totalSeconds: Long): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val rest = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, rest) else "%d:%02d".format(minutes, rest)
}

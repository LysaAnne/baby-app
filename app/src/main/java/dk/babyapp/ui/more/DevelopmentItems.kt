package dk.babyapp.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val developmentDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d. MMM yyyy", Locale.forLanguageTag("da"))

/** "3. nov. 2026" from a stored ISO date. */
internal fun developmentDate(iso: String): String = LocalDate.parse(iso).format(developmentDateFormat)

/** How a tick circle is read out: "Klaret: Vender sig" or "Ikke endnu: Vender sig". */
internal data class TickLabels(val done: String, val notDone: String)

internal val milestoneTick = TickLabels("Klaret", "Ikke endnu")

/**
 * The one card used in every Udvikling tab: a tick circle on the left (left out when [checked] is null), the title
 * with its age and status underneath, and an arrow on the right that folds the description out.
 */
@Composable
internal fun DevItemCard(
    id: String,
    title: String,
    subtitle: String,
    accent: Color,
    now: Boolean,
    checked: Boolean?,
    labels: TickLabels = milestoneTick,
    onToggle: () -> Unit = {},
    startOpen: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var open by rememberSaveable(id) { mutableStateOf(startOpen) }
    Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = if (checked == null) 12.dp else 4.dp, end = 12.dp, top = 4.dp, bottom = if (open) 10.dp else 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (checked != null) IconButton(onClick = onToggle) { TickIcon(checked, accent, title, labels) }
                Row(Modifier.weight(1f).clickable { open = !open }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleSmall)
                        if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (now) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.ExpandMore, if (open) "Skjul $title" else "Læs om $title", modifier = Modifier.rotate(if (open) 180f else 0f))
                }
            }
            if (open) Column(Modifier.padding(start = if (checked == null) 0.dp else 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        }
    }
}

@Composable
internal fun TickIcon(checked: Boolean, accent: Color, title: String, labels: TickLabels = milestoneTick) {
    if (checked) Icon(Icons.Filled.CheckCircle, "${labels.done}: $title", tint = accent)
    else Icon(Icons.Outlined.RadioButtonUnchecked, "${labels.notDone}: $title", tint = accent.copy(alpha = 0.45f))
}

/** A row in a Nu card that can be ticked off where it stands. */
@Composable
internal fun TickRow(title: String, checked: Boolean, accent: Color, labels: TickLabels = milestoneTick, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TickIcon(checked, accent, title, labels)
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

/** Section heading inside a tab: "Lige nu", "Tidslinje". */
@Composable
internal fun TabHeading(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

/** An age band on a timeline, in the accent colour when the child is in it. */
@Composable
internal fun BandHeading(text: String, active: Boolean, accent: Color) =
    Text(text, style = MaterialTheme.typography.titleSmall, color = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 6.dp))

/** A Nu card: a title with a link to its tab on the right, then a few lines. */
@Composable
internal fun NowCard(title: String, link: String, onLink: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = onLink) { Text(link) }
            }
            Column(Modifier.padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)
        }
    }
}

package dk.babyapp.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dk.babyapp.domain.DevProgress
import dk.babyapp.domain.DevState
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.tracking.FormDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val developmentDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d. MMM yyyy", Locale.forLanguageTag("da"))

/** "3. nov. 2026" from a stored ISO date. */
internal fun developmentDate(iso: String): String = LocalDate.parse(iso).format(developmentDateFormat)

/** How a tick circle is read out ("Klaret: Vender sig") and what the button in the popup says. */
internal data class TickLabels(val done: String, val notDone: String, val action: String)

internal val milestoneTick = TickLabels("Klaret", "Ikke endnu", "Barnet kan det")

/** Text and background for items left over from earlier: a warm amber, so they stand out from the current ones. */
internal val overdueText = Color(0xFFA35200)

/** The status word after the age on a card: "nu", "fra tidligere", "klaret". */
internal fun stateLabel(state: DevState, doneWord: String) = when (state) {
    DevState.Now -> "nu"
    DevState.Overdue -> "fra tidligere"
    DevState.DoneNow, DevState.DonePast -> doneWord
    DevState.Future -> null
}

/**
 * The one card used in every Udvikling tab: a tick circle on the left (left out when [checked] is null), the title
 * with its age and status underneath, and an arrow. Tapping the card opens the description as a popup.
 * Current items are white, items left over from earlier amber, done items greyed out and future items small and plain.
 */
@Composable
internal fun DevItemCard(
    title: String,
    subtitle: String,
    accent: Color,
    state: DevState,
    checked: Boolean?,
    labels: TickLabels = milestoneTick,
    onToggle: () -> Unit = {},
    popupSubtitle: String = subtitle,
    content: @Composable ColumnScope.() -> Unit,
) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    val future = state == DevState.Future
    val done = state == DevState.DoneNow || state == DevState.DonePast
    val background = when {
        future -> Color.White.copy(alpha = 0.55f)
        else -> Color.White
    }
    val subtitleColor = when (state) {
        DevState.Now -> accent
        DevState.Overdue -> overdueText
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    // Items left over from earlier get an amber outline, so they stand out from the current ones.
    val border = if (state == DevState.Overdue) androidx.compose.foundation.BorderStroke(1.5.dp, overdueText.copy(alpha = 0.7f)) else null
    Surface(shape = MaterialTheme.shapes.medium, color = background, border = border, modifier = Modifier.fillMaxWidth().alpha(if (done) 0.55f else 1f)) {
        Row(Modifier.fillMaxWidth().padding(start = if (checked == null) 12.dp else 4.dp, end = 8.dp, top = if (future) 0.dp else 4.dp, bottom = if (future) 0.dp else 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (checked != null) IconButton(onClick = onToggle) { TickIcon(checked, accent, title, labels) }
            Row(Modifier.weight(1f).clickable(onClickLabel = "Læs om $title") { open = true }.padding(vertical = if (future) 8.dp else 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = if (future) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall)
                    if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
                }
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Læs om $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (open) DevPopup(title, popupSubtitle, onClose = { open = false }, footer = checked?.let { isChecked ->
        {
            OutlinedButton(onClick = onToggle) {
                TickIcon(isChecked, accent, title, labels)
                Text(if (isChecked) labels.done else labels.action, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }, content = content)
}

/** A popup with a title, a scrollable text and Luk; [footer] sits to the left of Luk. */
@Composable
internal fun DevPopup(title: String, subtitle: String?, onClose: () -> Unit, footer: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    FormDialog(
        category = CareCategory.Measurement,
        onDismissRequest = onClose,
        title = { Text(title) },
        subtitle = subtitle?.takeIf { it.isNotBlank() },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp), content = content) },
        dismissButton = footer?.let { { it() } },
        confirmButton = { TextButton(onClick = onClose) { Text("Luk") } },
    )
}

@Composable
internal fun TickIcon(checked: Boolean, accent: Color, title: String, labels: TickLabels = milestoneTick) {
    if (checked) Icon(Icons.Filled.CheckCircle, "${labels.done}: $title", tint = accent)
    else Icon(Icons.Outlined.RadioButtonUnchecked, "${labels.notDone}: $title", tint = accent.copy(alpha = 0.45f))
}

/** Section heading inside a tab: "Lige nu · uge 6". */
@Composable
internal fun TabHeading(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

/** An age band among the future items, kept quiet. */
@Composable
internal fun BandHeading(text: String) =
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 6.dp))

/** "Om spring ?" at the top of a tab; opens the background text as a popup. */
@Composable
private fun AboutRow(title: String, content: @Composable ColumnScope.() -> Unit) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().clickable(onClickLabel = title) { open = true }.padding(horizontal = 4.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, tint = MaterialTheme.colorScheme.primary)
    }
    if (open) DevPopup(title, null, { open = false }, content = content)
}

/** A folded section heading: "Fremtidige spring (8)" with an arrow. */
@Composable
private fun FoldHeading(text: String, open: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(start = 4.dp, top = 10.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ExpandMore, if (open) "Skjul $text" else "Vis $text", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.rotate(if (open) 180f else 0f))
    }
}

/** One item in a tab: its key, where it stands, the age band it begins in and how to draw it. */
internal class DevEntry(val key: String, val state: DevState, val band: String, val card: @Composable () -> Unit)

/** Which folded sections are open; kept by the dialog and closed again when the tab changes. */
internal class DevFolds(val future: MutableState<Boolean>, val past: MutableState<Boolean>)

/**
 * The layout every tab shares: "Om …" with a question mark, "Lige nu" with the child's age and the current
 * items (left-over ones amber, done ones greyed), then "Fremtidige …" folded by age band, and "Afsluttede" folded.
 */
internal fun categoryItems(
    scope: LazyListScope,
    tab: String,
    aboutTitle: String,
    about: @Composable ColumnScope.() -> Unit,
    nowHeading: String,
    emptyNow: String,
    futureTitle: String,
    entries: List<DevEntry>,
    folds: DevFolds,
    nowNote: String? = null,
) = with(scope) {
    item(key = "$tab-about") { AboutRow(aboutTitle, about) }
    item(key = "$tab-now") { TabHeading(nowHeading) }
    val now = entries.filter { it.state == DevState.Now || it.state == DevState.Overdue } + entries.filter { it.state == DevState.DoneNow }
    if (nowNote != null) item(key = "$tab-note") { Text(nowNote, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp)) }
    else if (now.isEmpty()) item(key = "$tab-empty") { Text(emptyNow, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp)) }
    now.forEach { entry -> item(key = "$tab-now-${entry.key}") { entry.card() } }
    val future = entries.filter { it.state == DevState.Future }
    if (future.isNotEmpty()) {
        item(key = "$tab-future") { FoldHeading("$futureTitle (${future.size})", folds.future.value) { folds.future.value = !folds.future.value } }
        if (folds.future.value) future.groupBy { it.band }.forEach { (band, list) ->
            item(key = "$tab-band-$band") { BandHeading(band) }
            list.forEach { entry -> item(key = "$tab-future-${entry.key}") { entry.card() } }
        }
    }
    val past = entries.filter { it.state == DevState.DonePast }
    if (past.isNotEmpty()) {
        item(key = "$tab-past") { FoldHeading("Afsluttede (${past.size})", folds.past.value) { folds.past.value = !folds.past.value } }
        if (folds.past.value) past.forEach { entry -> item(key = "$tab-past-${entry.key}") { entry.card() } }
    }
}

/**
 * A card on Nu, the same for all four areas: the area's name and Se alle, a short summary of what is relevant at
 * this age, and a bar with how much of it is done ("2/6 lege og færdigheder").
 */
@Composable
internal fun NowSummaryCard(title: String, lines: List<String>, progress: DevProgress, unit: String, accent: Color, track: Color, onOpen: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = Color.White, modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Se alle $title", onClick = onOpen)) {
        Column(Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpen) { Text("Se alle"); Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null) }
            }
            Column(Modifier.padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                if (progress.total > 0) {
                    LinearProgressIndicator(
                        progress = { progress.done.toFloat() / progress.total },
                        color = accent, trackColor = track, drawStopIndicator = {},
                        modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp),
                    )
                    Text(if (progress.complete) "Alt klaret lige nu" else "${progress.done}/${progress.total} $unit",
                        style = MaterialTheme.typography.labelMedium, color = if (progress.complete) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

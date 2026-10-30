package dk.babyapp.ui.tracking

import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.preferences.HeadSideStatisticsSettings
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.domain.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Liggeside: which side the head lies on, to help avoid a flat side. One clear split at the top, the days below,
 * and the before/after comparison folded away.
 */
@Composable
internal fun HeadSideStatisticsDialog(childId: String, events: List<CareEventEntity>, settings: HeadSideStatisticsSettings, onSettingsChange: (HeadSideStatisticsSettings) -> Unit, onDismiss: () -> Unit) {
    var selection by remember(childId) { mutableStateOf(settings) }
    fun save(value: HeadSideStatisticsSettings) { selection = value; onSettingsChange(value) }
    val today = LocalDate.now()
    val period = StatisticsPeriod.entries.firstOrNull { it.name == selection.period } ?: StatisticsPeriod.Month
    val customFrom = runCatching { LocalDate.parse(selection.from) }.getOrNull() ?: today.minusMonths(1).plusDays(1)
    val customThrough = runCatching { LocalDate.parse(selection.through) }.getOrNull() ?: today
    val range = statisticsRange(period, today, customFrom, customThrough)
    val correction = runCatching { LocalDate.parse(selection.correctionDates[childId]) }.getOrNull()
    val valid = range.from <= range.through && range.through <= today
    val stats = remember(events, childId, range, correction) { if (valid) headSideStatistics(events, childId, range, correction) else null }
    val category = dk.babyapp.ui.theme.CareCategory.Sleep
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CategoryFormTheme(category) {
            Surface(Modifier.fillMaxSize(), color = formBody(category)) {
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(androidx.compose.material.icons.Icons.AutoMirrored.Outlined.ArrowBack, "Tilbage") }
                        Text("Liggeside", style = MaterialTheme.typography.titleLarge)
                    }
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // The same periods as the rest of Indsigt, fixed at the top while scrolling.
                        stickyHeader {
                            Box(Modifier.fillMaxWidth().background(formBody(category)).padding(vertical = 4.dp)) {
                                PeriodBar(StatisticsPeriod.entries.filter { it != StatisticsPeriod.Day }.map { it.name to it.label }, period.name) { save(selection.copy(period = it)) }
                            }
                        }
                        if (period == StatisticsPeriod.Custom) item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatisticsDateButton("Fra", customFrom, Modifier.weight(1f)) { save(selection.copy(from = it.toString())) }
                                StatisticsDateButton("Til", customThrough, Modifier.weight(1f)) { save(selection.copy(through = it.toString())) }
                            }
                        }
                        if (!valid) item { Text("Vælg en fra-dato før eller på til-datoen. Perioden må ikke ligge i fremtiden.", color = MaterialTheme.colorScheme.error) }
                        stats?.let { result ->
                            item {
                                val shownFrom = if (period == StatisticsPeriod.All) result.days.firstOrNull()?.date ?: range.through else range.from
                                SideSplitCard("Liggeside · hele perioden", "${shownFrom.shortDate()} – ${range.through.shortDate()}", result.total)
                            }
                            if (result.days.isNotEmpty()) item {
                                WhiteCard {
                                    Text("Pr. dag", style = MaterialTheme.typography.titleSmall)
                                    DaySplitBars(result.days.takeLast(31))
                                }
                            }
                            item {
                                WhiteCard {
                                    MoreDetails("sammenlign før og efter en dato", correction != null) {
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            StatisticsDateButton("Dato", correction, Modifier.weight(1f)) { date -> save(selection.copy(correctionDates = selection.correctionDates + (childId to date.toString()))) }
                                            if (correction != null) IconButton(onClick = { save(selection.copy(correctionDates = selection.correctionDates - childId)) }) {
                                                Icon(androidx.compose.material.icons.Icons.Outlined.Delete, "Fjern datoen", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Text("Fx den dag I begyndte at lægge barnet mere på den anden side. Registreringerne ændres ikke.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (correction != null) {
                                            if (correction < range.from || correction > range.through) Text("Datoen ligger uden for den valgte periode.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                            result.before?.let { SideSplitLine("Før ${correction.shortDate()}", it) }
                                            result.after?.let { SideSplitLine("Fra ${correction.shortDate()}", it) }
                                            val before = result.before?.leftPercent
                                            val after = result.after?.leftPercent
                                            if (before != null && after != null) Text("Venstre ændret med ${String.format(Locale.forLanguageTag("da"), "%+.0f", after - before)} procentpoint.", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                            item { Text("Procenterne viser andelen af registreringer, ikke hvor længe barnet har ligget på hver side.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                    LocalTimerBar.current()
                }
            }
        }
    }
}

/** A row of short period choices, the same everywhere in Indsigt: 1U, 1M, 3M … */
@Composable
internal fun PeriodBar(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (id, label) ->
            SegmentedButton(selected = id == selected, onClick = { onSelect(id) }, shape = SegmentedButtonDefaults.itemShape(index, options.size),
                icon = {}, colors = SegmentedButtonDefaults.colors(inactiveContainerColor = androidx.compose.ui.graphics.Color.White)) { Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1) }
        }
    }
}

@Composable
internal fun WhiteCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

private fun percent(value: Double?) = value?.let { String.format(Locale.forLanguageTag("da"), "%.0f %%", it) } ?: "–"

@Composable
private fun SideSplitCard(title: String, subtitle: String, counts: HeadSideTotals) {
    val accent = MaterialTheme.colorScheme.primary
    val light = MaterialTheme.colorScheme.primaryContainer
    WhiteCard {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Venstre", style = MaterialTheme.typography.labelLarge, color = accent)
                Text(percent(counts.leftPercent), style = MaterialTheme.typography.headlineMedium)
                Text("Venstre: ${counts.left}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.weight(1f), horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("Højre", style = MaterialTheme.typography.labelLarge, color = accent)
                Text(percent(counts.rightPercent), style = MaterialTheme.typography.headlineMedium)
                Text("Højre: ${counts.right}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // The split as one bar: left in the full colour, right in the light one.
        Row(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))) {
            if (counts.total == 0) Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            else {
                if (counts.left > 0) Box(Modifier.weight(counts.left.toFloat()).fillMaxHeight().background(accent))
                if (counts.right > 0) Box(Modifier.weight(counts.right.toFloat()).fillMaxHeight().background(light))
            }
        }
        Text(when {
            counts.total == 0 -> "Ingen registreringer i perioden"
            counts.left == counts.right -> "Lige meget på begge sider"
            counts.left > counts.right -> "Mest på venstre side"
            else -> "Mest på højre side"
        }, style = MaterialTheme.typography.bodyMedium)
        if (counts.unknown > 0) Text("${counts.unknown} uden angivet side er ikke talt med.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SideSplitLine(label: String, counts: HeadSideTotals) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("V ${percent(counts.leftPercent)} · H ${percent(counts.rightPercent)}", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Each day with registrations as a small bar split into left and right. */
@Composable
private fun DaySplitBars(days: List<HeadSideDay>) {
    val accent = MaterialTheme.colorScheme.primary
    val light = MaterialTheme.colorScheme.primaryContainer
    val max = days.maxOf { it.counts.total }.coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
        // At least 14 slots, so a few days give thin bars rather than one wide block.
        repeat((14 - days.size).coerceAtLeast(0)) { Spacer(Modifier.weight(1f)) }
        days.forEach { day ->
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                if (day.counts.right > 0) Box(Modifier.fillMaxWidth().fillMaxHeight(day.counts.right.toFloat() / max).background(light, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)))
                if (day.counts.left > 0) Box(Modifier.fillMaxWidth().height(90.dp * (day.counts.left.toFloat() / max)).background(accent))
            }
        }
    }
    Row(Modifier.fillMaxWidth()) {
        Text(days.first().date.shortDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(days.last().date.shortDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(accent, RoundedCornerShape(2.dp))); Text("venstre", style = MaterialTheme.typography.labelSmall)
        Box(Modifier.size(10.dp).background(light, RoundedCornerShape(2.dp))); Text("højre", style = MaterialTheme.typography.labelSmall)
    }
}

private fun LocalDate.shortDate() = format(DateTimeFormatter.ofPattern("dd.MM.yy"))

@Composable
private fun StatisticsDateButton(label: String, date: LocalDate?, modifier: Modifier = Modifier, onChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(modifier = modifier, onClick = {
        val initial = date ?: LocalDate.now()
        DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, initial.year, initial.monthValue - 1, initial.dayOfMonth).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }) { Text("$label: ${date?.shortDate() ?: "Vælg"}") }
}

package dk.babyapp.ui.tracking

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
    var showDays by remember { mutableStateOf(false) }
    val valid = range.from <= range.through && range.through <= today
    val stats = remember(events, childId, range, correction) { if (valid) headSideStatistics(events, childId, range, correction) else null }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nøgletal", style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onDismiss) { Text("Luk") }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatisticsPeriod.entries.forEach { option -> FilterChip(selected = period == option, onClick = { save(selection.copy(period = option.name)) }, label = { Text(option.label) }) }
                    } }
                    if (period == StatisticsPeriod.Custom) item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatisticsDateButton("Fra", customFrom, Modifier.weight(1f)) { save(selection.copy(from = it.toString())) }
                            StatisticsDateButton("Til", customThrough, Modifier.weight(1f)) { save(selection.copy(through = it.toString())) }
                        }
                    }
                    item { Text("${range.from.shortDate()} – ${range.through.shortDate()}", style = MaterialTheme.typography.bodySmall) }
                    if (!valid) item { Text("Vælg en fra-dato før eller på til-datoen. Perioden må ikke ligge i fremtiden.", color = MaterialTheme.colorScheme.error) }
                    stats?.let { result ->
                        item { HeadSideTotalsCard("Favoritside · hele perioden", result.total) }
                        item { Text("Procenterne viser andelen af registreringer, ikke hvor længe barnet ligger på hver side. Hyppigere registrering på én side påvirker fordelingen.", style = MaterialTheme.typography.bodySmall) }
                        item {
                            Row {
                                StatisticsDateButton("Korrektionsdato", correction, Modifier.weight(1f)) { date -> save(selection.copy(correctionDates = selection.correctionDates + (childId to date.toString()))) }
                                if (correction != null) TextButton(onClick = { save(selection.copy(correctionDates = selection.correctionDates - childId)) }) { Text("Fjern") }
                            }
                            Text("Datoen deler perioden. Registreringerne ændres ikke.", style = MaterialTheme.typography.bodySmall)
                        }
                        if (correction != null) {
                            if (correction < range.from || correction > range.through) item { Text("Korrektionsdatoen ligger uden for den valgte periode. Vælg en periode, der dækker begge sider af datoen.") }
                            item { HeadSideTotalsCard("Før korrektion · før ${correction.shortDate()}", requireNotNull(result.before)) }
                            item { HeadSideTotalsCard("Fra korrektion · fra ${correction.shortDate()}", requireNotNull(result.after)) }
                            item {
                                val before = result.before?.leftPercent
                                val after = result.after?.leftPercent
                                if (before != null && after != null) {
                                    Text("Ændring i venstre andel: ${String.format(Locale.forLanguageTag("da"), "%+.1f", after - before)} procentpoint. Perioderne kan have forskellig længde og antal registreringer.", style = MaterialTheme.typography.bodySmall)
                                } else Text("Der skal være registreringer både før og fra korrektionsdatoen for at sammenligne fordelingen.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        item { TextButton(onClick = { showDays = !showDays }) { Text(if (showDays) "Skjul akkumulerede tal" else "Vis akkumulerede tal pr. dag") } }
                        if (showDays) {
                            item { Text("Dage med registreringer. Totalerne akkumuleres fra periodens start.", style = MaterialTheme.typography.bodySmall) }
                            item { Row(Modifier.fillMaxWidth()) { Text("Dato", Modifier.weight(1f)); Text("Dagens V / H", Modifier.weight(1f)); Text("I alt V / H", Modifier.weight(1f)) } }
                            items(result.days) { day -> Row(Modifier.fillMaxWidth()) {
                                Text(day.date.shortDate(), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text("${day.counts.left} / ${day.counts.right}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text("${day.cumulative.left} / ${day.cumulative.right}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadSideTotalsCard(title: String, counts: HeadSideTotals) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("${counts.total} registreringer med angivet side")
            fun percent(value: Double?) = value?.let { String.format(Locale.forLanguageTag("da"), "%.1f %%", it) } ?: "–"
            Text("Venstre: ${counts.left} · ${percent(counts.leftPercent)}")
            Text("Højre: ${counts.right} · ${percent(counts.rightPercent)}")
            if (counts.total > 0) LinearProgressIndicator(progress = { counts.left.toFloat() / counts.total }, modifier = Modifier.fillMaxWidth())
            Text(when {
                counts.total == 0 -> "Ingen registreringer i perioden"
                counts.left == counts.right -> "Lige mange registreringer på begge sider"
                counts.left > counts.right -> "Flest registreringer: venstre"
                else -> "Flest registreringer: højre"
            }, style = MaterialTheme.typography.bodySmall)
            if (counts.unknown > 0) Text("${counts.unknown} uden angivet side er ikke med i procenterne.", style = MaterialTheme.typography.bodySmall)
        }
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

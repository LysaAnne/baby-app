package dk.babyapp.ui.insights

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.domain.calculateInsights
import dk.babyapp.domain.dailyInsightSeries
import dk.babyapp.domain.percentChange
import dk.babyapp.ui.components.BabyEmptyState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private enum class InsightTab(val label: String) { Overview("Overblik"), Feeding("Madning"), Sleep("Søvn"), Health("Ble og sundhed") }
private enum class DashboardStat(val label: String) { Sleep("Søvn"), Feedings("Madninger"), Diapers("Bleer"), TummyTime("Mavetid"), Bottle("Flaske"), LongestSleep("Længste søvn") }

@Composable
fun InsightsScreen(child: ChildProfile?, events: List<CareEventEntity>, preferences: AppPreferences, onUpdateDashboardMetrics: (List<String>) -> Unit, contentPadding: PaddingValues) {
    var through by remember { mutableStateOf(LocalDate.now()) }; var from by remember { mutableStateOf(through.minusDays(6)) }; var tab by remember { mutableStateOf(InsightTab.Overview) }
    var customizeDashboard by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault(); val childEvents = child?.let { selected -> events.filter { it.childId == selected.id } }.orEmpty(); val series = child?.let { dailyInsightSeries(events, it.id, from, through, zone) }.orEmpty()
    val current = child?.let { calculateInsights(events, it.id, from, through, zone) }; val days = ChronoUnit.DAYS.between(from, through).toInt() + 1; val previousThrough = from.minusDays(1); val previousFrom = previousThrough.minusDays(days - 1L); val previous = child?.let { calculateInsights(events, it.id, previousFrom, previousThrough, zone) }
    LazyColumn(contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Indsigt", style = MaterialTheme.typography.headlineSmall) }
        if (child == null) item { BabyEmptyState(Icons.Outlined.Assessment, "Vælg et barn", "Vælg et aktivt barn for at se grafer og statistik.") }
        else {
            val data = requireNotNull(current)
            val prior = requireNotNull(previous)
            item { PeriodSelector(from, through, { from = it.coerceAtMost(through) }, { through = it; if (from > it) from = it }) }
            item { PrimaryScrollableTabRow(selectedTabIndex = tab.ordinal, edgePadding = 0.dp) { InsightTab.entries.forEach { option -> Tab(selected = tab == option, onClick = { tab = option }, text = { Text(option.label) }) } } }
            when (tab) {
                InsightTab.Overview -> {
                    item { DataQualityCard(childEvents, from, through, zone) }
                    item { DashboardStatsCard(preferences.insightDashboardMetrics.mapNotNull { name -> DashboardStat.entries.firstOrNull { it.name == name } }, current!!, series.sumOf { it.tummyMinutes.toDouble() }.toLong()) { customizeDashboard = true } }
                    item { RhythmCard(childEvents, through, zone) }
                    item { ChartCard("Søvn pr. døgn", "Lure og nattesøvn vises separat.") { StackedBarChart(series.map { it.napMinutes }, series.map { it.nightMinutes }, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary, "Lur", "Nat") } }
                    item { ComparisonCard("Søvn", percentChange(data.sleepMinutes.toDouble(), prior.sleepMinutes.toDouble()), "${minutes(data.sleepMinutes)} i perioden") }
                    item { ComparisonCard("Madninger", percentChange(data.feedingCount.toDouble(), prior.feedingCount.toDouble()), "${data.feedingCount} registreringer") }
                    item { ComparisonCard("Bleer", percentChange(data.diaperCount.toDouble(), prior.diaperCount.toDouble()), "${data.diaperCount} registreringer") }
                    item { InfoCard() }
                }
                InsightTab.Feeding -> {
                    item { ChartCard("Madninger pr. døgn", "Antal registrerede amninger og flasker.") { BarChart(series.map { it.feedings }, MaterialTheme.colorScheme.primary) } }
                    item { ChartCard("Flaskemængde pr. døgn", "Samlet registreret mængde i ml.") { BarChart(series.map { it.bottleMl }, MaterialTheme.colorScheme.tertiary) } }
                    item { ChartCard("Amningstid pr. døgn", "Samlet registreret tid i minutter.") { LineChart(series.map { it.breastfeedingMinutes }, MaterialTheme.colorScheme.primary) } }
                    item { StatGrid(listOf("Madninger" to data.feedingCount.toString(), "Amning" to minutes(data.breastfeedingMinutes), "Flaske" to "${data.bottleMl} ml", "Gns. interval" to averageInterval(childEvents.filter { it.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle) }, from, through, zone))) }
                }
                InsightTab.Sleep -> {
                    item { ChartCard("Søvn pr. døgn", "Lure og nattesøvn i minutter.") { StackedBarChart(series.map { it.napMinutes }, series.map { it.nightMinutes }, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary, "Lur", "Nat") } }
                    item { StatGrid(listOf("Samlet søvn" to minutes(data.sleepMinutes), "Lure" to data.napCount.toString(), "Længste søvn" to minutes(data.longestSleepMinutes), "Flest starter" to data.commonSleepPeriod)) }
                    item { ComparisonCard("Samlet søvn", percentChange(data.sleepMinutes.toDouble(), prior.sleepMinutes.toDouble()), "Sammenlignet med de foregående $days dage") }
                }
                InsightTab.Health -> {
                    item { ChartCard("Bleer pr. døgn", "Våde bleer og afføring vises separat.") { StackedBarChart(series.map { it.wetDiapers }, series.map { it.dirtyDiapers }, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, "Våd", "Afføring") } }
                    item { StatGrid(listOf("Bleer" to data.diaperCount.toString(), "Våde" to data.wetDiapers.toString(), "Afføring" to data.dirtyDiapers.toString(), "Med observation" to data.diaperObservations.toString())) }
                    item { MeasurementChart("Temperatur", childEvents, MeasurementType.Temperature, from, through, zone, "°C") }
                    item { MeasurementChart("Vægt", childEvents, MeasurementType.Weight, from, through, zone, "kg") }
                    item { ChartCard("Mavetid pr. døgn", "Samlet registreret tid i minutter.") { BarChart(series.map { it.tummyMinutes }, MaterialTheme.colorScheme.secondary) } }
                    val medicineCount = childEvents.count { it.activityType?.name == "Medicine" && it.date(zone) in from..through }
                    item { ComparisonCard("Medicin", "$medicineCount registreringer", "Appen beregner eller anbefaler ikke doser.") }
                    item { InfoCard() }
                }
            }
        }
    }
    if (customizeDashboard) DashboardCustomizeDialog(preferences.insightDashboardMetrics.mapNotNull { name -> DashboardStat.entries.firstOrNull { it.name == name } }, { customizeDashboard = false }) { selected -> onUpdateDashboardMetrics(selected.map { it.name }); customizeDashboard = false }
}

@Composable private fun PeriodSelector(from: LocalDate, through: LocalDate, onFrom: (LocalDate) -> Unit, onThrough: (LocalDate) -> Unit) = Card { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Periode", style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(7, 14, 30).forEach { days -> FilterChip(selected = from == through.minusDays(days - 1L), onClick = { onFrom(through.minusDays(days - 1L)) }, label = { Text("$days dage") }) } }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { DateButton("Fra", from, onFrom); DateButton("Til", through, onThrough) } } }
@Composable private fun DateButton(label: String, value: LocalDate, onChange: (LocalDate) -> Unit) { val context = LocalContext.current; OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, value.year, value.monthValue - 1, value.dayOfMonth).show() }) { Text("$label: ${value.dayOfMonth}/${value.monthValue}") } }
@Composable private fun ChartCard(title: String, subtitle: String, chart: @Composable () -> Unit) = Card { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); chart() } }
@Composable private fun ComparisonCard(title: String, change: String, detail: String) = Card { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(change, style = MaterialTheme.typography.titleLarge); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun DashboardStatsCard(selected: List<DashboardStat>, data: dk.babyapp.domain.CareInsights, tummyMinutes: Long, onCustomize: () -> Unit) = Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Mine nøgletal", style = MaterialTheme.typography.titleMedium); IconButton(onClick = onCustomize) { Icon(Icons.Outlined.Tune, "Tilpas nøgletal") } }; val values = selected.map { stat -> stat.label to when (stat) { DashboardStat.Sleep -> minutes(data.sleepMinutes); DashboardStat.Feedings -> data.feedingCount.toString(); DashboardStat.Diapers -> data.diaperCount.toString(); DashboardStat.TummyTime -> minutes(tummyMinutes); DashboardStat.Bottle -> "${data.bottleMl} ml"; DashboardStat.LongestSleep -> minutes(data.longestSleepMinutes) } }; values.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { (label, value) -> Column(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium).padding(10.dp)) { Text(label, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.titleMedium) } } } } } }
@Composable private fun DashboardCustomizeDialog(current: List<DashboardStat>, onDismiss: () -> Unit, onSave: (List<DashboardStat>) -> Unit) { var selected by remember(current) { mutableStateOf(current.ifEmpty { DashboardStat.entries.take(4) }) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilpas nøgletal") }, text = { Column { Text("Vælg mellem 3 og 6 felter."); DashboardStat.entries.forEach { stat -> val checked = stat in selected; Row { Checkbox(checked, { choose -> if (choose && selected.size < 6) selected = selected + stat else if (!choose && selected.size > 3) selected = selected - stat }); Text(stat.label, Modifier.padding(top = 12.dp)) } } } }, confirmButton = { Button(onClick = { onSave(selected) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } }) }
@Composable private fun StatGrid(items: List<Pair<String, String>>) = Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { (label, value) -> Column(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium).padding(10.dp)) { Text(label, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.titleMedium) } } } } } }
@Composable private fun BarChart(values: List<Float>, color: Color) { val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f); Canvas(Modifier.fillMaxWidth().height(150.dp)) { val gap = 5.dp.toPx(); val width = (size.width - gap * (values.size - 1).coerceAtLeast(0)) / values.size.coerceAtLeast(1); values.forEachIndexed { index, value -> val h = size.height * value / max; drawRoundRect(color, Offset(index * (width + gap), size.height - h), Size(width, h), cornerRadius = CornerRadius(4.dp.toPx())) } } }
@Composable private fun StackedBarChart(first: List<Float>, second: List<Float>, firstColor: Color, secondColor: Color, firstLabel: String, secondLabel: String) { val totals = first.indices.map { first[it] + second.getOrElse(it) { 0f } }; val max = (totals.maxOrNull() ?: 0f).coerceAtLeast(1f); Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Canvas(Modifier.fillMaxWidth().height(150.dp)) { val gap = 5.dp.toPx(); val width = (size.width - gap * (first.size - 1).coerceAtLeast(0)) / first.size.coerceAtLeast(1); first.indices.forEach { index -> val h1 = size.height * first[index] / max; val h2 = size.height * second.getOrElse(index) { 0f } / max; val x = index * (width + gap); drawRect(firstColor, Offset(x, size.height - h1), Size(width, h1)); drawRect(secondColor, Offset(x, size.height - h1 - h2), Size(width, h2)) } }; Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { Legend(firstColor, firstLabel); Legend(secondColor, secondLabel) } } }
@Composable private fun Legend(color: Color, label: String) = Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { Box(Modifier.size(12.dp).background(color, MaterialTheme.shapes.extraSmall)); Text(label, style = MaterialTheme.typography.labelSmall) }
@Composable private fun LineChart(values: List<Float>, color: Color) { val max = (values.maxOrNull() ?: 0f).coerceAtLeast(1f); Canvas(Modifier.fillMaxWidth().height(150.dp)) { if (values.size == 1) drawCircle(color, 5.dp.toPx(), Offset(size.width / 2, size.height - size.height * values[0] / max)) else values.zipWithNext().forEachIndexed { index, (a, b) -> val step = size.width / (values.size - 1).coerceAtLeast(1); drawLine(color, Offset(index * step, size.height - size.height * a / max), Offset((index + 1) * step, size.height - size.height * b / max), 3.dp.toPx()) } } }
@Composable private fun RhythmCard(events: List<CareEventEntity>, date: LocalDate, zone: ZoneId) { val sleep = MaterialTheme.colorScheme.primary; val feeding = MaterialTheme.colorScheme.secondary; val diaper = MaterialTheme.colorScheme.tertiary; val colors = mapOf(CareEventType.Sleep to sleep, CareEventType.Breastfeeding to feeding, CareEventType.Bottle to feeding, CareEventType.Diaper to diaper); val selected = events.filter { it.date(zone) == date && it.type in colors }; ChartCard("Døgnrytme · ${date.dayOfMonth}/${date.monthValue}", "Søvn, madning og ble fordelt over 24 timer.") { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Canvas(Modifier.fillMaxWidth().height(110.dp)) { repeat(5) { line -> drawLine(Color.Gray.copy(alpha = .25f), Offset(size.width * line / 4f, 0f), Offset(size.width * line / 4f, size.height)) }; selected.forEach { event -> val start = Instant.ofEpochMilli(event.startedAt).atZone(zone); val startMinutes = start.hour * 60 + start.minute; val durationMinutes = ((event.endedAt ?: event.startedAt + 15 * 60_000) - event.startedAt).coerceAtLeast(10 * 60_000) / 60_000f; val lane = when (event.type) { CareEventType.Sleep -> 0; CareEventType.Breastfeeding, CareEventType.Bottle -> 1; else -> 2 }; drawRoundRect(colors.getValue(event.type), Offset(size.width * startMinutes / 1440f, lane * size.height / 3f + 4.dp.toPx()), Size((size.width * durationMinutes / 1440f).coerceAtLeast(5.dp.toPx()), size.height / 3f - 8.dp.toPx()), CornerRadius(4.dp.toPx())) } }; Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Legend(sleep, "Søvn"); Legend(feeding, "Madning"); Legend(diaper, "Ble") }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("00", style = MaterialTheme.typography.labelSmall); Text("06", style = MaterialTheme.typography.labelSmall); Text("12", style = MaterialTheme.typography.labelSmall); Text("18", style = MaterialTheme.typography.labelSmall); Text("24", style = MaterialTheme.typography.labelSmall) } } } }
@Composable private fun MeasurementChart(title: String, events: List<CareEventEntity>, type: MeasurementType, from: LocalDate, through: LocalDate, zone: ZoneId, unit: String) { val values = events.filter { it.type == CareEventType.Measurement && it.measurementType == type && it.date(zone) in from..through }.sortedBy { it.startedAt }.mapNotNull { it.measurementValue?.toFloat() }; ChartCard(title, if (values.isEmpty()) "Ingen målinger i perioden" else "${values.size} målinger · seneste ${values.last()} $unit") { LineChart(values, MaterialTheme.colorScheme.tertiary) } }
@Composable private fun DataQualityCard(events: List<CareEventEntity>, from: LocalDate, through: LocalDate, zone: ZoneId) { val days = ChronoUnit.DAYS.between(from, through).toInt() + 1; val covered = events.map { it.date(zone) }.distinct().count { it in from..through }; ComparisonCard("Datagrundlag", "$covered af $days dage", if (covered == days) "Der er registreringer på alle dage i perioden." else "Dage uden registreringer kan påvirke grafer og gennemsnit.") }
@Composable private fun InfoCard() = Card { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("Sådan læses oversigten", style = MaterialTheme.typography.titleMedium); Text("Graferne beskriver kun de oplysninger, du har registreret. De er ikke en vurdering af barnets sundhed, trivsel eller udvikling.") } }
private fun CareEventEntity.date(zone: ZoneId) = Instant.ofEpochMilli(startedAt).atZone(zone).toLocalDate()
private fun minutes(value: Long) = if (value < 60) "$value min" else "${value / 60} t ${value % 60} min"
private fun averageInterval(events: List<CareEventEntity>, from: LocalDate, through: LocalDate, zone: ZoneId): String { val sorted = events.filter { it.date(zone) in from..through }.sortedBy { it.startedAt }; if (sorted.size < 2) return "-"; val average = sorted.zipWithNext().map { (a, b) -> b.startedAt - a.startedAt }.average().toLong() / 60_000; return minutes(average) }

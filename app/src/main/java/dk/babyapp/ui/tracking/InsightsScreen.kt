package dk.babyapp.ui.tracking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.preferences.HeadSideStatisticsSettings
import dk.babyapp.data.preferences.InsightCard
import dk.babyapp.data.preferences.MeasurementChartSettings
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.domain.DayInsight
import dk.babyapp.domain.PeriodInsights
import dk.babyapp.domain.periodInsights
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val danish = Locale.forLanguageTag("da")

private val InsightCard.icon: ImageVector get() = when (this) {
    InsightCard.Sleep -> CareCategory.Sleep.icon
    InsightCard.Feeding -> CareCategory.Feeding.icon
    InsightCard.Diapers -> CareCategory.Diaper.icon
    InsightCard.Rhythm -> Icons.Outlined.Schedule
    InsightCard.Growth -> CareCategory.Measurement.icon
    InsightCard.HeadSide -> Icons.Outlined.SwapHoriz
}

private val InsightCard.category: CareCategory get() = when (this) {
    InsightCard.Sleep, InsightCard.Rhythm, InsightCard.HeadSide -> CareCategory.Sleep
    InsightCard.Feeding -> CareCategory.Feeding
    InsightCard.Diapers -> CareCategory.Diaper
    InsightCard.Growth -> CareCategory.Measurement
}

internal fun formatMinutesShort(minutes: Long) = if (minutes >= 60) "${minutes / 60} t ${"%02d".format(minutes % 60)} m" else "$minutes m"
private fun decimal(value: Double) = String.format(danish, "%.1f", value)

/**
 * Indsigt: patterns in the child's registrations over the last 7 or 30 days, as cards the user
 * orders and switches on and off. Numbers only describe the registrations; nothing is graded.
 */
@Composable
internal fun InsightsScreen(
    childId: String?,
    events: List<CareEventEntity>,
    contentPadding: PaddingValues,
    cards: List<InsightCard>,
    periodDays: Int,
    onPeriodChange: (Int) -> Unit,
    onCardsChange: (List<InsightCard>) -> Unit,
    chartSettings: MeasurementChartSettings = MeasurementChartSettings(),
    onChartSettingsChange: (MeasurementChartSettings) -> Unit = {},
    statisticsSettings: HeadSideStatisticsSettings = HeadSideStatisticsSettings(),
    onStatisticsSettingsChange: (HeadSideStatisticsSettings) -> Unit = {},
) {
    var detail by rememberSaveable(childId) { mutableStateOf<InsightCard?>(null) }
    var customizeOpen by remember { mutableStateOf(false) }
    val insights = remember(events, childId, periodDays) { childId?.let { periodInsights(events, it, periodDays) } }
    LazyColumn(
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 4.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Indsigt", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = { customizeOpen = true }) { Icon(Icons.Outlined.Tune, "Tilpas Indsigt") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7, 30).forEach { days -> FilterChip(periodDays == days, { onPeriodChange(days) }, { Text("$days dage", style = MaterialTheme.typography.labelLarge) }) }
            }
        }
        if (insights == null) item { Text("Opret eller vælg et barn for at se indsigt.") }
        else items(cards.size, key = { cards[it].name }) { index ->
            val card = cards[index]
            InsightCardFrame(card, onOpen = { detail = card }) { InsightCardContent(card, insights) }
        }
        if (insights != null) item {
            Text(
                "Tallene viser mønstre i dine egne registreringer og er ikke en vurdering af, hvad der er normalt.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
    if (childId != null && insights != null) when (detail) {
        InsightCard.Growth -> MeasurementChartsDialog(events, childId, chartSettings, onChartSettingsChange) { detail = null }
        InsightCard.HeadSide -> HeadSideStatisticsDialog(childId, events, statisticsSettings, onStatisticsSettingsChange) { detail = null }
        null -> Unit
        else -> InsightDetail(detail!!, insights, periodDays, onPeriodChange) { detail = null }
    }
    if (customizeOpen) InsightCardsDialog(cards, { customizeOpen = false }) { onCardsChange(it); customizeOpen = false }
}

@Composable
private fun InsightCardFrame(card: InsightCard, onOpen: () -> Unit, content: @Composable () -> Unit) {
    val colors = MaterialTheme.babyColors.category(card.category)
    Surface(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.clickable(onClickLabel = "Vis detaljer", onClick = onOpen).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(28.dp).background(colors.container, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(card.icon, null, tint = colors.accent, modifier = Modifier.size(16.dp))
                }
                Text(card.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun Headline(value: String, detail: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp).weight(1f))
    }
}

@Composable
private fun InsightCardContent(card: InsightCard, insights: PeriodInsights) {
    val accent = MaterialTheme.babyColors.category(card.category).accent
    val light = MaterialTheme.babyColors.category(card.category).container
    val empty = insights.days.all { it.sleepMinutes == 0L && it.feedings == 0 && it.diapers == 0 }
    when (card) {
        InsightCard.Sleep -> if (insights.days.all { it.sleepMinutes == 0L }) NoData() else {
            Headline(formatMinutesShort(insights.avgSleepMinutes), "pr. døgn i snit · lur ${formatMinutesShort(insights.avgNapMinutes)} · nat ${formatMinutesShort(insights.avgNightMinutes)}")
            DayBars(insights.days, accent, light, height = 40.dp, lower = { it.nightMinutes.toFloat() }, upper = { it.napMinutes.toFloat() })
        }
        InsightCard.Feeding -> if (insights.days.all { it.feedings == 0 }) NoData() else {
            Headline(decimal(insights.avgFeedings), "madninger pr. døgn · amning ${formatMinutesShort(insights.avgNursingMinutes)} · flaske ${insights.avgBottleMl} ml")
            DayBars(insights.days, accent, light, height = 28.dp, lower = { it.feedings.toFloat() })
        }
        InsightCard.Diapers -> if (insights.days.all { it.diapers == 0 }) NoData() else {
            Headline(decimal(insights.avgDiapers), "bleer pr. døgn · våde ${decimal(insights.avgWet)} · afføring ${decimal(insights.avgDirty)}")
            DayBars(insights.days, accent, light, height = 28.dp, lower = { it.diapers.toFloat() })
        }
        InsightCard.Rhythm -> if (empty) NoData() else RhythmStrips(insights.days.takeLast(3))
        InsightCard.Growth -> GrowthSummary(insights)
        InsightCard.HeadSide -> {
            val total = insights.headLeft + insights.headRight
            if (total == 0) NoData() else {
                Row(Modifier.fillMaxWidth().height(10.dp).background(light, RoundedCornerShape(5.dp))) {
                    if (insights.headLeft > 0) Box(Modifier.weight(insights.headLeft.toFloat()).height(10.dp).background(accent, RoundedCornerShape(5.dp)))
                    if (insights.headRight > 0) Box(Modifier.weight(insights.headRight.toFloat()).height(10.dp))
                }
                Row(Modifier.fillMaxWidth()) {
                    Text("Venstre ${insights.headLeft * 100 / total} %", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("Højre ${insights.headRight * 100 / total} %", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun NoData() = Text("Ingen registreringer i perioden", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun GrowthSummary(insights: PeriodInsights) {
    val shown = listOf(MeasurementType.Weight to "Vægt", MeasurementType.Height to "Længde", MeasurementType.HeadCircumference to "Hoved")
        .mapNotNull { (type, label) -> insights.latest[type]?.let { Triple(type, label, it) } }
    if (shown.isEmpty()) { NoData(); return }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        shown.forEach { (type, label, m) ->
            Column(Modifier.weight(1f)) {
                Text("${String.format(danish, "%.1f", m.value).removeSuffix(",0")} ${m.unit}", style = MaterialTheme.typography.titleMedium)
                val change = insights.weightChange.takeIf { type == MeasurementType.Weight }
                Text(
                    change?.let { (if (it >= 0) "+" else "") + (if (m.unit == "kg") "${(it * 1000).toInt()} g" else String.format(danish, "%.0f %s", it, m.unit)) + " i perioden" }
                        ?: "$label · ${java.time.Instant.ofEpochMilli(m.at).atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("d. MMM", danish))}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** One bar per day; today, which isn't over yet, is drawn lighter. [upper] stacks on top of [lower]. */
@Composable
private fun DayBars(days: List<DayInsight>, color: Color, todayColor: Color, height: Dp, lower: (DayInsight) -> Float, upper: (DayInsight) -> Float = { 0f }, labels: Boolean = true) {
    val max = days.maxOfOrNull { lower(it) + upper(it) }?.takeIf { it > 0f } ?: 1f
    val upperColor = todayColor
    Canvas(Modifier.fillMaxWidth().height(height).semantics { contentDescription = "Graf over ${days.size} dage" }) {
        val gap = if (days.size > 10) 2.dp.toPx() else 4.dp.toPx()
        val w = (size.width - gap * (days.size - 1)) / days.size
        days.forEachIndexed { i, day ->
            val x = i * (w + gap)
            val today = i == days.lastIndex
            val lowH = lower(day) / max * size.height
            val upH = upper(day) / max * size.height
            val radius = CornerRadius(3.dp.toPx())
            if (upH > 0f) drawRoundRect(if (today) upperColor.copy(alpha = 0.6f) else upperColor, Offset(x, size.height - lowH - upH), Size(w, upH), radius)
            if (lowH > 0f) drawRoundRect(if (today) color.copy(alpha = 0.45f) else color, Offset(x, size.height - lowH), Size(w, lowH), radius)
        }
    }
    if (labels) Row(Modifier.fillMaxWidth()) {
        if (days.size <= 7) days.forEachIndexed { i, day ->
            Text(if (i == days.lastIndex) "i dag" else day.date.format(DateTimeFormatter.ofPattern("EEE", danish)).removeSuffix("."), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        } else {
            Text(days.first().date.format(DateTimeFormatter.ofPattern("d. MMM", danish)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("i dag", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Each day from midnight to midnight: sleep as bars, feedings as small marks. */
@Composable
private fun RhythmStrips(days: List<DayInsight>, showDates: Boolean = false) {
    val sleep = MaterialTheme.babyColors.category(CareCategory.Sleep).accent
    val feed = MaterialTheme.babyColors.category(CareCategory.Feeding).accent
    val track = MaterialTheme.babyColors.category(CareCategory.Sleep).container
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = if (showDates) 56.dp else 0.dp)) {
            listOf("00", "06", "12", "18", "24").forEachIndexed { i, hour ->
                Text(hour, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = if (i < 4) Modifier.weight(1f) else Modifier)
            }
        }
        days.forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDates) Text(day.date.format(DateTimeFormatter.ofPattern("EEE d.", danish)), style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp))
                Canvas(Modifier.weight(1f).height(10.dp)) {
                    drawRoundRect(track, size = size, cornerRadius = CornerRadius(3.dp.toPx()))
                    day.sleepBlocks.forEach { block -> drawRect(sleep, Offset(block.start * size.width, 0f), Size(((block.endInclusive - block.start) * size.width).coerceAtLeast(1f), size.height)) }
                    day.feedingMarks.forEach { mark -> drawRect(feed, Offset(mark * size.width - 1.5.dp.toPx(), 0f), Size(3.dp.toPx(), size.height)) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Legend(sleep, "søvn"); Legend(feed, "madning")
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    Box(Modifier.size(8.dp).background(color, CircleShape)); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun StatGrid(stats: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { (label, value) ->
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(value, style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InsightDetail(card: InsightCard, insights: PeriodInsights, periodDays: Int, onPeriodChange: (Int) -> Unit, onClose: () -> Unit) {
    val accent = MaterialTheme.babyColors.category(card.category).accent
    val light = MaterialTheme.babyColors.category(card.category).container
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Tilbage") }
                    Text(card.label, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    listOf(7, 30).forEach { days -> FilterChip(periodDays == days, { onPeriodChange(days) }, { Text("$days dage") }, modifier = Modifier.padding(end = 6.dp)) }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    @Composable fun Section(content: @Composable () -> Unit) = Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() } }
                    when (card) {
                        InsightCard.Sleep -> {
                            Section {
                                Text("Søvn pr. døgn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                DayBars(insights.days, accent, light, height = 120.dp, lower = { it.nightMinutes.toFloat() }, upper = { it.napMinutes.toFloat() })
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Legend(accent, "nat"); Legend(light, "lur") }
                            }
                            Section {
                                StatGrid(listOf(
                                    "Søvn i alt pr. døgn" to formatMinutesShort(insights.avgSleepMinutes),
                                    "Nattesøvn" to formatMinutesShort(insights.avgNightMinutes),
                                    "Lure pr. dag" to decimal(insights.napsPerDay),
                                    "Lur i alt pr. dag" to formatMinutesShort(insights.avgNapMinutes),
                                    "Længste søvn" to formatMinutesShort(insights.longestSleepMinutes),
                                    "Opvågninger pr. nat" to (insights.avgAwakenings?.let(::decimal) ?: "–"),
                                ))
                            }
                            Section {
                                Text("Typisk sengetid", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val clock = DateTimeFormatter.ofPattern("HH.mm")
                                Text(insights.bedtime?.let { if (it.start == it.endInclusive) it.start.format(clock) else "${it.start.format(clock)}–${it.endInclusive.format(clock)}" } ?: "Registrér mindst 3 nætter for at se den", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        InsightCard.Feeding -> {
                            Section {
                                Text("Madninger pr. døgn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                DayBars(insights.days, accent, light, height = 120.dp, lower = { it.feedings.toFloat() })
                            }
                            Section {
                                StatGrid(listOf(
                                    "Madninger pr. døgn" to decimal(insights.avgFeedings),
                                    "Amning pr. døgn" to formatMinutesShort(insights.avgNursingMinutes),
                                    "Flaske pr. døgn" to "${insights.avgBottleMl} ml",
                                ))
                            }
                            Section {
                                Text("Flaske pr. døgn (ml)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                DayBars(insights.days, accent, light, height = 80.dp, lower = { it.bottleMl.toFloat() })
                            }
                        }
                        InsightCard.Diapers -> {
                            Section {
                                Text("Bleer pr. døgn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                DayBars(insights.days, accent, light, height = 120.dp, lower = { it.wetDiapers.toFloat() }, upper = { it.dirtyDiapers.toFloat() })
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Legend(accent, "våd"); Legend(light, "afføring") }
                            }
                            Section {
                                StatGrid(listOf(
                                    "Bleer pr. døgn" to decimal(insights.avgDiapers),
                                    "Våde pr. døgn" to decimal(insights.avgWet),
                                    "Afføring pr. døgn" to decimal(insights.avgDirty),
                                ))
                            }
                        }
                        InsightCard.Rhythm -> Section { RhythmStrips(insights.days.reversed(), showDates = true) }
                        else -> Unit
                    }
                    Text(
                        "Gennemsnit er beregnet over ${insights.averagedDays} hele ${if (insights.averagedDays == 1) "dag" else "dage"} med registreringer. Tallene viser mønstre i dine egne registreringer og er ikke en vurdering af, hvad der er normalt.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCardsDialog(current: List<InsightCard>, onDismiss: () -> Unit, onSave: (List<InsightCard>) -> Unit) {
    // Shown cards first in their order, then the hidden ones.
    var order by remember { mutableStateOf(current + InsightCard.entries.filterNot { it in current }) }
    var shown by remember { mutableStateOf(current.toSet()) }
    FormDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tilpas Indsigt") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Vælg kortene, og flyt dem op eller ned.", style = MaterialTheme.typography.bodyMedium)
                order.forEachIndexed { index, card ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Switch(card in shown, { on -> shown = if (on) shown + card else shown - card }, modifier = Modifier.semantics { contentDescription = "Vis ${card.label}" })
                        Text(card.label, modifier = Modifier.weight(1f).padding(start = 12.dp))
                        IconButton(enabled = index > 0, onClick = { order = order.toMutableList().apply { add(index - 1, removeAt(index)) } }) { Icon(Icons.Outlined.ArrowUpward, "Flyt ${card.label} op") }
                        IconButton(enabled = index < order.lastIndex, onClick = { order = order.toMutableList().apply { add(index + 1, removeAt(index)) } }) { Icon(Icons.Outlined.ArrowDownward, "Flyt ${card.label} ned") }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(order.filter { it in shown }) }) { Text("Gem") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

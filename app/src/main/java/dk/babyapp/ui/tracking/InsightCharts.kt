package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.babyapp.domain.DayInsight
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val danishLocale = Locale.forLanguageTag("da")

/** The period shown in Indsigt: a number of days, everything (0), or chosen dates (-1). */
internal data class InsightPeriodState(val days: Int, val from: String?, val through: String?)

/** Every period choice in Indsigt, the same on every page. */
internal val insightPeriodChoices = listOf(7 to "1U", 30 to "1M", 90 to "3M", 180 to "6M", 365 to "1Å", 0 to "Alt", -1 to "Dato")

/** The period bar with Alt and Dato; Dato shows two date buttons below it. Meant to sit fixed at the top. */
@Composable
internal fun InsightPeriodBar(period: InsightPeriodState, range: Pair<LocalDate, LocalDate>, onDays: (Int) -> Unit, onRange: (LocalDate, LocalDate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PeriodBar(insightPeriodChoices.map { it.first.toString() to it.second }, period.days.toString()) { onDays(it.toInt()) }
        if (period.days == -1) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateChoiceButton("Fra", range.first, Modifier.weight(1f)) { onRange(it, maxOf(it, range.second)) }
            DateChoiceButton("Til", range.second, Modifier.weight(1f)) { onRange(minOf(range.first, it), it) }
        }
    }
}

@Composable
private fun DateChoiceButton(label: String, date: LocalDate, modifier: Modifier, onChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(modifier = modifier, onClick = {
        DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, date.year, date.monthValue - 1, date.dayOfMonth).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }) { Text("$label ${date.format(DateTimeFormatter.ofPattern("d. MMM yy", danishLocale))}") }
}

/** How a bar chart groups the days: each day, averages per weekday, per month of the year, or per year. */
internal enum class Grouping(val label: String) { Day("Pr. døgn"), Weekday("Ugedag"), Month("Måned"), Year("År") }

internal data class Bar(val label: String, val value: Float, val detail: String)

/**
 * Groups [days] for a chart. Weekday, month and year show the average per day over the period's finished
 * days where that kind was registered, so a short month, a part-year or a day without registrations is not under-counted. Today, which isn't over, only counts in Pr. døgn.
 */
internal fun groupBars(days: List<DayInsight>, grouping: Grouping, value: (DayInsight) -> Float, format: (Float) -> String): List<Bar> {
    val today = LocalDate.now()
    val finished = days.filter { it.date < today }.ifEmpty { days }
    fun averaged(groups: Map<*, List<DayInsight>>, label: (Any?) -> String) = groups.map { (key, list) ->
        // Only days where something of this kind was registered count in the average.
        val registered = list.filter { value(it) > 0f }
        val avg = if (registered.isEmpty()) 0f else registered.map(value).average().toFloat()
        Bar(label(key), avg, "${label(key)}: ${format(avg)} i snit (${registered.size} ${if (registered.size == 1) "dag" else "dage"} med registreringer)")
    }
    return when (grouping) {
        // A long period ("Alt", 3M) starts at the first day something of this kind was registered, not at birth.
        Grouping.Day -> (if (days.size <= 7) days else days.dropWhile { value(it) <= 0f }).let { shown -> shown.map { day ->
            val label = if (day.date == today) "i dag" else day.date.format(DateTimeFormatter.ofPattern("EEE d. MMM", danishLocale))
            Bar(if (shown.size <= 7) (if (day.date == today) "i dag" else day.date.format(DateTimeFormatter.ofPattern("EEE", danishLocale)).removeSuffix(".")) else "", value(day), "$label: ${format(value(day))}")
        } }
        Grouping.Weekday -> averaged(finished.groupBy { it.date.dayOfWeek }.toSortedMap()) { (it as DayOfWeek).getDisplayName(TextStyle.SHORT, danishLocale).removeSuffix(".") }
        Grouping.Month -> averaged(finished.groupBy { it.date.monthValue }.toSortedMap()) { java.time.Month.of(it as Int).getDisplayName(TextStyle.SHORT, danishLocale).removeSuffix(".") }
        Grouping.Year -> averaged(finished.groupBy { it.date.year }.toSortedMap()) { it.toString() }
    }
}

/** Grouping choices as a small bar of their own. */
@Composable
internal fun GroupingBar(selected: Grouping, onSelect: (Grouping) -> Unit) {
    PeriodBar(Grouping.entries.map { it.name to it.label }, selected.name) { onSelect(Grouping.valueOf(it)) }
}

/**
 * Bars that can be tapped: the tapped bar is outlined and its value shown above the chart.
 * [labels] under the bars are shown when there are few enough to read.
 */
@Composable
internal fun TapBars(bars: List<Bar>, color: Color, height: Dp = 120.dp, hint: String = "Tryk på en søjle for at se tallet") {
    var selected by remember(bars) { mutableStateOf<Int?>(null) }
    val outline = MaterialTheme.colorScheme.onSurface
    Text(selected?.let { bars.getOrNull(it)?.detail } ?: hint, style = MaterialTheme.typography.labelMedium,
        color = if (selected != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
    if (bars.isEmpty()) return
    val max = bars.maxOf { it.value }.takeIf { it > 0f } ?: 1f
    Canvas(Modifier.fillMaxWidth().height(height).semantics { contentDescription = "Søjlediagram med ${bars.size} søjler" }.pointerInput(bars) {
        detectTapGestures { offset -> selected = (offset.x / size.width * bars.size).toInt().coerceIn(0, bars.lastIndex) }
    }) {
        val gap = if (bars.size > 20) 1.dp.toPx() else if (bars.size > 10) 2.dp.toPx() else 4.dp.toPx()
        val w = ((size.width - gap * (bars.size - 1)) / bars.size).coerceAtLeast(1f)
        bars.forEachIndexed { i, bar ->
            val h = bar.value / max * size.height
            val x = i * (w + gap)
            if (h > 0f) drawRoundRect(if (selected == null || selected == i) color else color.copy(alpha = 0.45f), Offset(x, size.height - h), Size(w, h), CornerRadius(3.dp.toPx()))
            if (selected == i) drawRoundRect(outline, Offset(x, size.height - h.coerceAtLeast(2f)), Size(w, h.coerceAtLeast(2f)), CornerRadius(3.dp.toPx()), style = Stroke(1.5.dp.toPx()))
        }
    }
    if (bars.size <= 12 && bars.any { it.label.isNotBlank() }) Row(Modifier.fillMaxWidth()) {
        bars.forEach { bar -> Text(bar.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1) }
    }
}

/**
 * Døgnrytme, zoomable: pinch to stretch the day sideways, drag to move along it, tap a sleep or a feeding to
 * see its times. Each row is one day from midnight to midnight.
 */
@Composable
internal fun RhythmZoom(days: List<DayInsight>, sleep: Color, feed: Color, track: Color) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableFloatStateOf(0f) }
    var picked by remember(days) { mutableStateOf<String?>(null) }
    val rowHeight = 16.dp
    val gap = 6.dp
    val axis = MaterialTheme.colorScheme.onSurfaceVariant
    val clock = DateTimeFormatter.ofPattern("HH.mm")
    fun timeOf(date: LocalDate, fraction: Float) = date.atStartOfDay().plusMinutes((fraction * 1440).toLong()).format(clock)
    Text(picked ?: "Knib for at zoome, træk for at flytte, og tryk på en søvn eller madning.", style = MaterialTheme.typography.labelMedium,
        color = if (picked != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
    Row {
        Column(Modifier.width(52.dp).padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(gap)) {
            days.forEach { day -> Text(day.date.format(DateTimeFormatter.ofPattern("EEE d.", danishLocale)), style = MaterialTheme.typography.labelSmall, modifier = Modifier.heightIn(min = rowHeight)) }
        }
        Canvas(Modifier.weight(1f).height(18.dp + (rowHeight + gap) * days.size)
            .pointerInput(days) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val newZoom = (zoom * gestureZoom).coerceIn(1f, 8f)
                    val content = size.width * newZoom
                    offset = (offset * (newZoom / zoom) - pan.x).coerceIn(0f, content - size.width)
                    zoom = newZoom
                }
            }
            .pointerInput(days, zoom, offset) {
                detectTapGestures { tap ->
                    val row = ((tap.y - 18.dp.toPx()) / (rowHeight + gap).toPx()).toInt()
                    val day = days.getOrNull(row) ?: return@detectTapGestures
                    val fraction = (tap.x + offset) / (size.width * zoom)
                    val tolerance = 6.dp.toPx() / (size.width * zoom)
                    val mark = day.feedingMarks.firstOrNull { kotlin.math.abs(it - fraction) <= tolerance }
                    val block = day.sleepBlocks.firstOrNull { fraction in it }
                    picked = when {
                        mark != null -> "Madning ${day.date.format(DateTimeFormatter.ofPattern("d. MMM", danishLocale))} kl. ${timeOf(day.date, mark)}"
                        block != null -> {
                            val minutes = ((block.endInclusive - block.start) * 1440).toLong()
                            "Søvn ${day.date.format(DateTimeFormatter.ofPattern("d. MMM", danishLocale))} ${timeOf(day.date, block.start)}–${timeOf(day.date, block.endInclusive)} · ${formatMinutesShort(minutes)}"
                        }
                        else -> null
                    }
                }
            }) {
            val width = size.width * zoom
            fun x(fraction: Float) = fraction * width - offset
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(200, (axis.red * 255).toInt(), (axis.green * 255).toInt(), (axis.blue * 255).toInt()); textSize = 10.dp.toPx() }
            // Hour marks: every 6 hours at normal size, every hour when zoomed in.
            val step = when { zoom >= 4f -> 1; zoom >= 2f -> 3; else -> 6 }
            for (hour in 0..24 step step) {
                val hx = x(hour / 24f)
                if (hx < -20f || hx > size.width + 20f) continue
                drawContext.canvas.nativeCanvas.drawText("%02d".format(hour), hx.coerceIn(0f, size.width - 14.dp.toPx()), 12.dp.toPx(), paint)
                drawLine(axis.copy(alpha = 0.15f), Offset(hx, 16.dp.toPx()), Offset(hx, size.height))
            }
            days.forEachIndexed { i, day ->
                val top = 18.dp.toPx() + i * (rowHeight + gap).toPx()
                val h = rowHeight.toPx()
                drawRoundRect(track, Offset(x(0f), top), Size(width, h), CornerRadius(3.dp.toPx()))
                day.sleepBlocks.forEach { block -> drawRect(sleep, Offset(x(block.start), top), Size(((block.endInclusive - block.start) * width).coerceAtLeast(1f), h)) }
                day.feedingMarks.forEach { mark -> drawRect(feed, Offset(x(mark) - 1.5.dp.toPx(), top), Size(3.dp.toPx(), h)) }
            }
        }
    }
}

package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ChartPeriod(val label: String) { Week("En uge"), Month("En måned"), SixMonths("6 måneder"), Year("Et år"), All("Al tid"), Custom("Valgfri periode") }

@Composable
internal fun MeasurementChartsDialog(events: List<CareEventEntity>, childId: String, settings: dk.babyapp.data.preferences.MeasurementChartSettings = dk.babyapp.data.preferences.MeasurementChartSettings(), onSettingsChange: (dk.babyapp.data.preferences.MeasurementChartSettings) -> Unit = {}, onDismiss: () -> Unit) {
    var period by remember { mutableStateOf(ChartPeriod.entries.firstOrNull { it.name == settings.period } ?: ChartPeriod.Month) }
    val today = LocalDate.now()
    var customFrom by remember { mutableStateOf(runCatching { LocalDate.parse(settings.from) }.getOrNull() ?: today.minusMonths(1)) }
    var customThrough by remember { mutableStateOf(runCatching { LocalDate.parse(settings.through) }.getOrNull() ?: today) }
    fun saveSelection() = onSettingsChange(dk.babyapp.data.preferences.MeasurementChartSettings(period.name, customFrom.toString(), customThrough.toString()))
    val from = when (period) {
        ChartPeriod.Week -> today.minusDays(6)
        ChartPeriod.Month -> today.minusMonths(1).plusDays(1)
        ChartPeriod.SixMonths -> today.minusMonths(6).plusDays(1)
        ChartPeriod.Year -> today.minusYears(1).plusDays(1)
        ChartPeriod.All -> null
        ChartPeriod.Custom -> customFrom
    }
    val through = if (period == ChartPeriod.Custom) customThrough else today
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Målenes udvikling", style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onDismiss) { Text("Luk") }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item { SelectionDropdown("Periode", period.label, ChartPeriod.entries.map { it to it.label }) { period = it; saveSelection() } }
                    if (period == ChartPeriod.Custom) item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChartDateButton("Fra", customFrom) { customFrom = it; saveSelection() }
                            ChartDateButton("Til", customThrough) { customThrough = it; saveSelection() }
                        }
                    }
                    if (from != null && from > through) item { Text("Fra-dato skal være før eller samme dag som til-dato.", color = MaterialTheme.colorScheme.error) }
                    else items(MeasurementType.entries) { type ->
                        val points = remember(events, childId, type, from, through) { measurementPoints(events, childId, type, from, through) }
                        MeasurementChartCard(type, points, from, through)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartDateButton(label: String, date: LocalDate, onChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }) {
        Text("$label ${date.format(DateTimeFormatter.ofPattern("dd.MM.yy"))}")
    }
}

@Composable
private fun MeasurementChartCard(type: MeasurementType, points: List<MeasurementPoint>, from: LocalDate?, through: LocalDate) {
    val temperature = type == MeasurementType.Temperature
    val unit = type.defaultUnit()
    val dateFormat = DateTimeFormatter.ofPattern("d/M")
    val zone = ZoneId.systemDefault()
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val primary = MaterialTheme.colorScheme.primary
    val axis = MaterialTheme.colorScheme.onSurface
    var details by remember { mutableStateOf(false) }
    val uri = LocalUriHandler.current
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${type.displayLabel()} ($unit)", style = MaterialTheme.typography.titleMedium)
            if (points.isEmpty()) Text("Ingen målinger i perioden", style = MaterialTheme.typography.bodyMedium)
            else {
                val minValue = if (temperature) minOf(35.0, points.minOf { it.value } - 0.5) else points.minOf { it.value }
                val maxValue = if (temperature) maxOf(40.0, points.maxOf { it.value } + 0.5) else points.maxOf { it.value }
                val margin = if (temperature) 0.0 else maxOf((maxValue - minValue) * 0.12, 0.1)
                val low = minValue - margin
                val high = maxValue + margin
                val start = (from ?: Instant.ofEpochMilli(points.first().at).atZone(zone).toLocalDate()).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = through.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Canvas(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = "${type.displayLabel()}: ${points.size} målinger, fra ${points.first().value} til ${points.last().value} $unit" }) {
                    val left = 46.dp.toPx(); val top = 8.dp.toPx(); val bottom = size.height - 26.dp.toPx(); val right = size.width - 8.dp.toPx()
                    fun x(at: Long) = left + ((at - start).toDouble() / (end - start).coerceAtLeast(1)).toFloat() * (right - left)
                    fun y(value: Double) = bottom - ((value - low) / (high - low)).toFloat() * (bottom - top)
                    if (temperature) {
                        drawRect(Color(0xFF66A780).copy(alpha = 0.22f), Offset(left, y(37.5)), Size(right - left, y(36.5) - y(37.5)))
                        drawLine(Color(0xFFB54B4B), Offset(left, y(38.0)), Offset(right, y(38.0)), 1.dp.toPx())
                    }
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(255, (axis.red * 255).toInt(), (axis.green * 255).toInt(), (axis.blue * 255).toInt()); textSize = 10.dp.toPx() }
                    listOf(low, (low + high) / 2, high).forEach { value ->
                        drawLine(axis.copy(alpha = 0.15f), Offset(left, y(value)), Offset(right, y(value)))
                        drawContext.canvas.nativeCanvas.drawText(String.format(locale, "%.1f", value), 0f, y(value) + 4.dp.toPx(), paint)
                    }
                    if (temperature) points.forEach { point ->
                        val color = when { point.value >= 38 -> Color(0xFFB54B4B); point.value > 37.5 -> Color(0xFFB47B24); point.value < 36.5 -> Color(0xFF4380B1); else -> Color(0xFF4E8C65) }
                        val width = minOf(14.dp.toPx(), (right - left) / points.size.coerceAtLeast(1) * 0.7f).coerceAtLeast(2.dp.toPx())
                        drawRect(color, Offset((x(point.at) - width / 2).coerceAtLeast(left), y(point.value)), Size(width, (bottom - y(point.value)).coerceAtLeast(1f)))
                    } else {
                        val path = Path()
                        points.forEachIndexed { index, point -> if (index == 0) path.moveTo(x(point.at), y(point.value)) else path.lineTo(x(point.at), y(point.value)) }
                        drawPath(path, primary, style = Stroke(2.dp.toPx()))
                        points.forEach { drawCircle(primary, 3.dp.toPx(), Offset(x(it.at), y(it.value))) }
                    }
                    drawContext.canvas.nativeCanvas.drawText(Instant.ofEpochMilli(start).atZone(zone).format(dateFormat), left, size.height, paint)
                    paint.textAlign = android.graphics.Paint.Align.RIGHT
                    drawContext.canvas.nativeCanvas.drawText(through.format(dateFormat), right, size.height, paint)
                }
                Text("Seneste: ${String.format(locale, "%.2f", points.last().value)} $unit · ${points.size} målinger", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { details = !details }) { Text(if (details) "Skjul målinger" else "Vis målinger") }
                if (details) points.forEach { point ->
                    Text("${Instant.ofEpochMilli(point.at).atZone(zone).format(DateTimeFormatter.ofPattern(if (point.timeSpecified) "dd.MM.yy HH:mm" else "dd.MM.yy"))} · ${String.format(locale, "%.2f", point.value)} $unit", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (temperature) {
                Text("Grøn: vejledende normalområde 36,5–37,5 °C. Blå: under området. Gul: over området. Rød: ≥38 °C (feber).", style = MaterialTheme.typography.bodySmall)
                Text("Gælder rektal måling; målemetoden er ikke registreret. Temperaturen varierer over dagen og kan normalt være op til 37,9 °C om aftenen. Kontakt læge ved feber hos børn under 3 måneder.", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { uri.openUri("https://www.sundhed.dk/borger/patienthaandbogen/boern/symptomer/feber-hos-smaaboern/") }) { Text("Om temperatur og feber") }
            }
        }
    }
}

package dk.babyapp.ui.tracking

import dk.babyapp.ui.theme.babyColors
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*
import dk.babyapp.ui.theme.BabySpacing
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class ChartPeriod(val label: String) { Week("En uge"), Month("En måned"), SixMonths("6 måneder"), Year("Et år"), All("Al tid"), Custom("Valgfri periode") }

/** WHO tables from assets/growth, read once per file. */
private object GrowthTables {
    private val cache = ConcurrentHashMap<String, GrowthReference>()
    fun get(context: Context, file: String): GrowthReference = cache.getOrPut(file) {
        GrowthReference.parse(context.assets.open("growth/$file").bufferedReader().use { it.readText() })
    }
}

@Composable
internal fun MeasurementChartsDialog(events: List<CareEventEntity>, childId: String, settings: dk.babyapp.data.preferences.MeasurementChartSettings = dk.babyapp.data.preferences.MeasurementChartSettings(), onSettingsChange: (dk.babyapp.data.preferences.MeasurementChartSettings) -> Unit = {}, child: ChildProfile? = null, onDismiss: () -> Unit) {
    var period by remember { mutableStateOf(ChartPeriod.entries.firstOrNull { it.name == settings.period } ?: ChartPeriod.Month) }
    var showGrowth by remember { mutableStateOf(settings.showGrowthReferences) }
    val today = LocalDate.now()
    var customFrom by remember { mutableStateOf(runCatching { LocalDate.parse(settings.from) }.getOrNull() ?: today.minusMonths(1)) }
    var customThrough by remember { mutableStateOf(runCatching { LocalDate.parse(settings.through) }.getOrNull() ?: today) }
    fun saveSelection() = onSettingsChange(dk.babyapp.data.preferences.MeasurementChartSettings(period.name, customFrom.toString(), customThrough.toString(), showGrowth))
    val from = when (period) {
        ChartPeriod.Week -> today.minusDays(6)
        ChartPeriod.Month -> today.minusMonths(1).plusDays(1)
        ChartPeriod.SixMonths -> today.minusMonths(6).plusDays(1)
        ChartPeriod.Year -> today.minusYears(1).plusDays(1)
        ChartPeriod.All -> null
        ChartPeriod.Custom -> customFrom
    }
    val through = if (period == ChartPeriod.Custom) customThrough else today
    val availability = child?.growthAvailability()
    val growth = (availability as? GrowthAvailability.Ready)?.takeIf { showGrowth }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(start = BabySpacing.xs, end = BabySpacing.l, top = BabySpacing.s), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Luk") }
                    Column(Modifier.weight(1f)) {
                        Text("Vækst", style = MaterialTheme.typography.titleLarge)
                        if (child != null) Text(listOf(child.name, child.ageLabel(today)).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                LazyColumn(contentPadding = PaddingValues(start = BabySpacing.l, end = BabySpacing.l, top = BabySpacing.s, bottom = BabySpacing.xl), verticalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
                    if (child != null) item {
                        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = BabySpacing.l, vertical = BabySpacing.s), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Vis vækstkurver", style = MaterialTheme.typography.titleSmall)
                                    Text("WHO for vægt, længde og hovedomkreds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(showGrowth, { showGrowth = it; saveSelection() }, modifier = Modifier.semantics { contentDescription = "Vis vækstkurver" })
                            }
                        }
                    }
                    item {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
                            ChartPeriod.entries.forEach { option -> FilterChip(selected = period == option, onClick = { period = option; saveSelection() }, label = { Text(option.label) }) }
                        }
                    }
                    if (period == ChartPeriod.Custom) item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChartDateButton("Fra", customFrom) { customFrom = it; saveSelection() }
                            ChartDateButton("Til", customThrough) { customThrough = it; saveSelection() }
                        }
                    }
                    if (showGrowth && child != null) item { GrowthNotice(child, availability) }
                    if (from != null && from > through) item { Text("Fra-dato skal være før eller samme dag som til-dato.", color = MaterialTheme.colorScheme.error) }
                    else items(MeasurementType.entries) { type ->
                        val points = remember(events, childId, type, from, through) { measurementPoints(events, childId, type, from, through) }
                        MeasurementChartCard(type, points, from, through, child?.takeIf { growth != null }, growth)
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowthNotice(child: ChildProfile, availability: GrowthAvailability?) {
    val (icon, title, text) = when (availability) {
        is GrowthAvailability.Ready -> Triple(Icons.Outlined.Info, "Sådan læses kurverne",
            "Linjerne viser, hvordan ${if (availability.sex == GrowthSex.Girl) "piger" else "drenge"} på samme alder vokser. p50 er midten, ikke et mål. Det vigtigste er, at barnet følger sin egen kurve. Tal med sundhedsplejersken, hvis du er i tvivl. Percentiler vises fra 14 dage til 2 år.")
        GrowthAvailability.MissingSex -> Triple(Icons.Outlined.PersonSearch, "Vælg køn i profilen",
            "Kurverne er forskellige for piger og drenge. Udfyld køn under Familie → ${child.name}. Målingerne vises stadig.")
        GrowthAvailability.MissingBirthDate -> Triple(Icons.Outlined.PersonSearch, "Fødselsdato mangler",
            "Kurverne bruger barnets alder. Udfyld fødselsdatoen under Familie → ${child.name}.")
        is GrowthAvailability.Premature -> Triple(Icons.Outlined.Info, "Ingen kurver endnu",
            "${child.name} er født i uge ${availability.weeks}. For tidligt fødte skal sammenlignes ud fra korrigeret alder, og det understøtter appen ikke endnu. Målingerne vises som før.")
        null -> return
    }
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
        Row(Modifier.fillMaxWidth().padding(BabySpacing.m), horizontalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

/** Short age for the chart axis: weeks for the first two months, then months. */
private fun axisAge(birth: LocalDate, date: LocalDate): String? {
    val days = ageDays(birth, date)
    if (days < 0) return null
    if (days < 61) return "${days / 7} uger"
    val months = Period.between(birth, date).toTotalMonths()
    return if (months == 1L) "1 md." else "$months mdr."
}

/** The birth measurement from the profile; drawn with the curves but never stored as a registration. */
private fun birthPoint(child: ChildProfile, type: MeasurementType, zone: ZoneId): MeasurementPoint? {
    val birth = child.birthDate ?: return null
    val value = when (type) {
        MeasurementType.Weight -> child.birthWeightGrams?.let { it / 1000.0 }
        MeasurementType.Height -> child.birthLengthCm
        MeasurementType.HeadCircumference -> child.birthHeadCircumferenceCm
        MeasurementType.Temperature -> null
    }?.takeIf { it.isFinite() && it > 0 } ?: return null
    return MeasurementPoint(birth.atTime(12, 0).atZone(zone).toInstant().toEpochMilli(), value, timeSpecified = false)
}

@Composable
private fun MeasurementChartCard(type: MeasurementType, points: List<MeasurementPoint>, from: LocalDate?, through: LocalDate, child: ChildProfile? = null, growth: GrowthAvailability.Ready? = null) {
    val temperature = type == MeasurementType.Temperature
    val unit = type.defaultUnit()
    val dateFormat = DateTimeFormatter.ofPattern("d/M")
    val zone = ZoneId.systemDefault()
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val primary = MaterialTheme.colorScheme.primary
    val axis = MaterialTheme.colorScheme.onSurface
    var details by remember { mutableStateOf(false) }
    val uri = LocalUriHandler.current
    val context = LocalContext.current
    val file = growth?.let { type.growthFile(it.sex) }
    val reference by produceState<GrowthReference?>(null, file) {
        value = file?.let { withContext(Dispatchers.IO) { runCatching { GrowthTables.get(context, it) }.getOrNull() } }
    }
    val curves = growth != null && reference != null
    fun dateOf(at: Long) = Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
    val birthPoint = if (curves && child != null) birthPoint(child, type, zone)?.takeIf { point -> dateOf(point.at).let { (from == null || it >= from) && it <= through } } else null
    val shown = listOfNotNull(birthPoint) + points
    fun format(value: Double, decimals: Int = 2) = String.format(locale, "%.${decimals}f", value)
    OutlinedCard(Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${type.displayLabel()} ($unit)", style = MaterialTheme.typography.titleMedium)
            if (shown.isEmpty()) Text("Ingen målinger i perioden", style = MaterialTheme.typography.bodyMedium)
            else {
                val start = (from ?: dateOf(shown.first().at)).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = through.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                // Reference lines are sampled along the same time axis as the measurements.
                val lines: List<Pair<Int, List<Pair<Long, Double>?>>> = if (curves) remember(reference, growth, start, end) {
                    growthPercentiles.map { percentile ->
                        val z = normalQuantile(percentile.toDouble())
                        // One sample per table day (or every few days over long periods), placed at noon.
                        val first = dateOf(start); val days = ageDays(first, dateOf(end - 1))
                        val step = maxOf(1L, days / 150)
                        percentile to (0..days step step).map { offset ->
                            val date = first.plusDays(offset)
                            reference!!.at(ageDays(growth!!.birth, date))?.valueAt(z)?.let { date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli() to it }
                        }
                    }
                } else emptyList()
                val lineValues = lines.flatMap { (_, samples) -> samples.mapNotNull { it?.second } }
                val minValue = if (temperature) minOf(35.0, shown.minOf { it.value } - 0.5) else (shown.map { it.value } + lineValues).min()
                val maxValue = if (temperature) maxOf(40.0, shown.maxOf { it.value } + 0.5) else (shown.map { it.value } + lineValues).max()
                val margin = if (temperature) 0.0 else maxOf((maxValue - minValue) * 0.08, 0.1)
                val low = minValue - margin
                val high = maxValue + margin
                val chartColors = MaterialTheme.babyColors
                val description = "${type.displayLabel()}: ${shown.size} målinger, fra ${format(shown.first().value)} til ${format(shown.last().value)} $unit"
                Canvas(Modifier.fillMaxWidth().height(if (curves) 220.dp else 190.dp).semantics { contentDescription = description }) {
                    val left = 46.dp.toPx(); val top = 8.dp.toPx(); val bottom = size.height - 26.dp.toPx(); val right = size.width - (if (curves) 30.dp else 8.dp).toPx()
                    fun x(at: Long) = left + ((at - start).toDouble() / (end - start).coerceAtLeast(1)).toFloat() * (right - left)
                    fun y(value: Double) = bottom - ((value - low) / (high - low)).toFloat() * (bottom - top)
                    if (temperature) {
                        drawRect(chartColors.temperatureNormalBand.copy(alpha = 0.22f), Offset(left, y(37.5)), Size(right - left, y(36.5) - y(37.5)))
                        drawLine(chartColors.temperatureFever, Offset(left, y(38.0)), Offset(right, y(38.0)), 1.dp.toPx())
                    }
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.argb(255, (axis.red * 255).toInt(), (axis.green * 255).toInt(), (axis.blue * 255).toInt()); textSize = 10.dp.toPx() }
                    listOf(low, (low + high) / 2, high).forEach { value ->
                        drawLine(axis.copy(alpha = 0.15f), Offset(left, y(value)), Offset(right, y(value)))
                        drawContext.canvas.nativeCanvas.drawText(String.format(locale, "%.1f", value), 0f, y(value) + 4.dp.toPx(), paint)
                    }
                    val labelPaint = android.graphics.Paint(paint).apply { color = android.graphics.Color.argb(150, (axis.red * 255).toInt(), (axis.green * 255).toInt(), (axis.blue * 255).toInt()) }
                    lines.forEach { (percentile, samples) ->
                        val path = Path(); var drawing = false; var last: Pair<Long, Double>? = null
                        samples.forEach { sample ->
                            if (sample == null) { drawing = false; return@forEach }
                            if (drawing) path.lineTo(x(sample.first), y(sample.second)) else path.moveTo(x(sample.first), y(sample.second))
                            drawing = true; last = sample
                        }
                        val median = percentile == 50
                        drawPath(path, primary.copy(alpha = if (median) 0.4f else 0.2f), style = Stroke(if (median) 1.5.dp.toPx() else 1.dp.toPx(), pathEffect = if (median) PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())) else null))
                        last?.takeIf { percentile in setOf(3, 50, 97) }?.let { drawContext.canvas.nativeCanvas.drawText("p$percentile", x(it.first) + 3.dp.toPx(), y(it.second) + 3.dp.toPx(), labelPaint) }
                    }
                    if (temperature) points.forEach { point ->
                        val color = when { point.value >= 38 -> chartColors.temperatureFever; point.value > 37.5 -> chartColors.temperatureElevated; point.value < 36.5 -> chartColors.temperatureLow; else -> chartColors.temperatureNormal }
                        val width = minOf(14.dp.toPx(), (right - left) / points.size.coerceAtLeast(1) * 0.7f).coerceAtLeast(2.dp.toPx())
                        drawRect(color, Offset((x(point.at) - width / 2).coerceAtLeast(left), y(point.value)), Size(width, (bottom - y(point.value)).coerceAtLeast(1f)))
                    } else {
                        val path = Path()
                        shown.forEachIndexed { index, point -> if (index == 0) path.moveTo(x(point.at), y(point.value)) else path.lineTo(x(point.at), y(point.value)) }
                        drawPath(path, primary, style = Stroke(if (curves) 2.5.dp.toPx() else 2.dp.toPx()))
                        shown.forEach { drawCircle(primary, 3.dp.toPx(), Offset(x(it.at), y(it.value))) }
                        birthPoint?.let { drawCircle(Color.White, 1.5.dp.toPx(), Offset(x(it.at), y(it.value))) }
                    }
                    val startDate = dateOf(start)
                    drawContext.canvas.nativeCanvas.drawText((if (curves) axisAge(growth!!.birth, startDate) else null) ?: startDate.format(dateFormat), left, size.height, paint)
                    paint.textAlign = android.graphics.Paint.Align.RIGHT
                    drawContext.canvas.nativeCanvas.drawText((if (curves) axisAge(growth!!.birth, through) else null) ?: through.format(dateFormat), right, size.height, paint)
                }
                if (curves) GrowthSummaryText(type, points, reference!!, growth!!.birth, ::dateOf, ::format)
                else Text("Seneste: ${format(points.last().value)} $unit · ${points.size} målinger", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { details = !details }) { Text(if (details) "Skjul målinger" else "Vis målinger") }
                if (details) {
                    birthPoint?.let { Text("${dateOf(it.at).format(DateTimeFormatter.ofPattern("dd.MM.yy"))} · ${format(it.value)} $unit · ved fødslen (fra profilen)", style = MaterialTheme.typography.bodySmall) }
                    points.forEach { point ->
                        Text("${Instant.ofEpochMilli(point.at).atZone(zone).format(DateTimeFormatter.ofPattern(if (point.timeSpecified) "dd.MM.yy HH:mm" else "dd.MM.yy"))} · ${format(point.value)} $unit", style = MaterialTheme.typography.bodySmall)
                    }
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

/** Placement and development described plainly; never a verdict on how the child is doing. */
@Composable
private fun GrowthSummaryText(type: MeasurementType, points: List<MeasurementPoint>, reference: GrowthReference, birth: LocalDate, dateOf: (Long) -> LocalDate, format: (Double, Int) -> String) {
    val unit = type.defaultUnit()
    val summary = remember(points, reference, birth) { growthSummary(points.map { GrowthMeasurement(dateOf(it.at), it.value) }, reference, birth) }
    if (points.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${format(points.last().value, 2)} $unit", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            summary?.let { Text(percentileLabel(it.latestZ).let { label -> if (label.startsWith("p")) "omkring $label" else label }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small).padding(horizontal = 8.dp, vertical = 2.dp)) }
        }
        val dayFormat = DateTimeFormatter.ofPattern("d. MMM", Locale.forLanguageTag("da"))
        fun gain(value: Double) = if (type == MeasurementType.Weight) "${if (value >= 0) "+" else "−"}${kotlin.math.abs(value * 1000).toInt()} g" else "${if (value >= 0) "+" else "−"}${format(kotlin.math.abs(value), 1)} cm"
        val small = MaterialTheme.typography.bodySmall
        val muted = MaterialTheme.colorScheme.onSurfaceVariant
        when {
            summary == null -> Text("Percentiler vises for målinger fra 14 dage til 2 år.", style = small, color = muted)
            summary.first == null -> Text("Én måling viser placering, ikke udvikling.", style = small, color = muted)
            else -> {
                val firstLabel = percentileLabel(summary.firstZ!!); val latestLabel = percentileLabel(summary.latestZ)
                Text(if (firstLabel == latestLabel) "Omkring $latestLabel siden ${summary.first.date.format(dayFormat)}" else "Fra $firstLabel til $latestLabel siden ${summary.first.date.format(dayFormat)}", style = small, color = muted)
                if (summary.measuredGain != null && summary.sameCurveGain != null) Text("Tilvækst: ${gain(summary.measuredGain)} · langs samme kurve: ${gain(summary.sameCurveGain)}", style = small, color = muted)
            }
        }
    }
}

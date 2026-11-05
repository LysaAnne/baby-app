package dk.babyapp.ui.more

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import dk.babyapp.domain.psychAgeBand
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.domain.Leap
import dk.babyapp.domain.LeapPhase
import dk.babyapp.domain.developmentStart
import dk.babyapp.domain.leapPhase
import dk.babyapp.domain.leaps
import dk.babyapp.domain.milestones
import dk.babyapp.domain.plusWeekFraction
import dk.babyapp.domain.weeksFromDue
import dk.babyapp.domain.window
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import dk.babyapp.ui.tracking.CategoryFormTheme
import dk.babyapp.ui.tracking.LocalTimerBar
import dk.babyapp.ui.tracking.PeriodBar
import dk.babyapp.ui.tracking.WhiteCard
import dk.babyapp.ui.tracking.formBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal enum class DevelopmentTab(val label: String) { Now("Nu"), Leaps("Spring"), Milestones("Milepæle"), Psychology("Psykologi"), Experiments("Eksperimenter") }

private val dateFormat = DateTimeFormatter.ofPattern("d. MMM yyyy", Locale.forLanguageTag("da"))

/**
 * Udvikling: the ten development spurts counted from the due date, with what to look for, games, what can be
 * hard and skills to tick off, plus milestones. Ticked items are kept per child with the date.
 */
@Composable
internal fun DevelopmentDialog(child: ChildProfile, checks: List<DevelopmentCheck>, onUpdateChecks: (List<DevelopmentCheck>) -> Unit, onDismiss: () -> Unit) {
    val today = LocalDate.now()
    val start = developmentStart(child)
    val weeks = weeksFromDue(child, today)
    val mine = checks.filter { it.childId == child.id }.associateBy { it.key }
    // Always toggle against the latest list: the tick rows can hold on to an older toggle function, and toggling
    // against an old list dropped earlier ticks and made a tick impossible to remove.
    val latestChecks by androidx.compose.runtime.rememberUpdatedState(checks)
    val latestUpdate by androidx.compose.runtime.rememberUpdatedState(onUpdateChecks)
    fun toggle(key: String) {
        val current = latestChecks
        val existing = current.firstOrNull { it.childId == child.id && it.key == key }
        latestUpdate(if (existing != null) current - existing else current + DevelopmentCheck(child.id, key, today.toString()))
    }
    var tab by rememberSaveable { mutableStateOf(DevelopmentTab.Now) }
    val colors = MaterialTheme.babyColors.category(CareCategory.Measurement)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CategoryFormTheme(CareCategory.Measurement) {
            Surface(Modifier.fillMaxSize(), color = formBody(CareCategory.Measurement)) {
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Tilbage") }
                        Text("Udvikling", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    }
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (start == null || weeks == null) {
                            item { WhiteCard { Text("Angiv terminsdato eller fødselsdato på barnets profil under Familie, så vises udviklingen her.") } }
                            return@LazyColumn
                        }
                        val phase = leapPhase(weeks)
                        item {
                            WhiteCard {
                                Text("Uge ${weeks.toInt().coerceAtLeast(0)} fra termin", style = MaterialTheme.typography.titleLarge)
                                Text(when (phase) {
                                    is LeapPhase.InLeap -> "Spring ${phase.leap.number} · ${phase.leap.title}. Ofte en urolig periode, til ca. ${start.plusWeekFraction(phase.leap.calmFromWeek).format(dateFormat)}."
                                    is LeapPhase.Calm -> phase.next?.let { "Rolig periode. Spring ${it.number} begynder ca. ${start.plusWeekFraction(it.fussyFromWeek).format(dateFormat)}." } ?: "Alle ti spring er overstået."
                                }, style = MaterialTheme.typography.bodyMedium)
                                LeapStrip(weeks, colors.accent, colors.container, legend = true)
                            }
                        }
                        // Five tabs don't fit on one row, so they sit in two: the leaps and milestones, then psychology and experiments.
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(DevelopmentTab.entries.take(3), DevelopmentTab.entries.drop(3)).forEach { row ->
                                    PeriodBar(row.map { it.name to it.label }, tab.name) { tab = DevelopmentTab.valueOf(it) }
                                }
                            }
                        }
                        val months = weeks * 7 / 30.44
                        when (tab) {
                            DevelopmentTab.Now -> nowItems(this, phase, weeks, months, mine, colors.accent, ::toggle) { tab = it }
                            DevelopmentTab.Leaps -> {
                                val current = (phase as? LeapPhase.InLeap)?.leap ?: (phase as LeapPhase.Calm).next
                                if (current != null) {
                                    item { TabHeading(if (phase is LeapPhase.InLeap) "Lige nu" else "Næste spring") }
                                    item(key = "leap-now") { LeapCard(current, "leap-now-${current.number}", start, weeks, mine, colors.accent, ::toggle) }
                                }
                                item { TabHeading("Tidslinje") }
                                leaps.groupBy { psychAgeBand(it.fussyFromWeek * 7 / 30.44) }.forEach { (band, list) ->
                                    item(key = "leap-band-$band") { BandHeading(band, list.any { weeks >= it.fussyFromWeek && weeks < it.calmFromWeek }, colors.accent) }
                                    list.forEach { leap -> item(key = "leap-${leap.number}") { LeapCard(leap, "leap-${leap.number}", start, weeks, mine, colors.accent, ::toggle) } }
                                }
                            }
                            DevelopmentTab.Milestones -> {
                                item { Text("Kryds en milepæl af, når barnet kan den. Vinduet viser, hvornår de fleste børn når den.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp)) }
                                val now = milestones.filter { it.isNow(months) }
                                if (now.isNotEmpty()) {
                                    item { TabHeading("Lige nu") }
                                    now.forEach { m -> item(key = "ms-now-${m.key}") { MilestoneCard(m, "ms-now-${m.key}", months, mine, colors.accent, ::toggle) } }
                                }
                                item { TabHeading("Tidslinje") }
                                milestones.sortedBy { it.fromMonths }.groupBy { psychAgeBand(it.fromMonths) }.forEach { (band, list) ->
                                    item(key = "ms-band-$band") { BandHeading(band, list.any { it.isNow(months) }, colors.accent) }
                                    list.forEach { m -> item(key = "ms-${m.key}") { MilestoneCard(m, "ms-${m.key}", months, mine, colors.accent, ::toggle) } }
                                }
                            }
                            DevelopmentTab.Psychology -> psychologyItems(this, months, mine, colors.accent, ::toggle)
                            DevelopmentTab.Experiments -> experimentItems(this, months, mine, colors.accent, ::toggle)
                        }
                        item {
                            Text("Springene er en model: alle børn udvikler sig i deres eget tempo, og ugerne er kun omtrentlige. Tal med sundhedsplejersken, hvis du er bekymret.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(4.dp))
                        }
                    }
                    LocalTimerBar.current()
                }
            }
        }
    }
}

/** What to look for, games, what can be hard and skills; games and skills can be ticked off. */
@Composable
private fun LeapDetails(leap: Leap, checks: Map<String, DevelopmentCheck>, accent: Color, onToggle: (String) -> Unit, inCard: Boolean = false) {
    @Composable fun Section(title: String, content: @Composable () -> Unit) {
        if (inCard) Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, style = MaterialTheme.typography.titleSmall); content() }
        else WhiteCard { Text(title, style = MaterialTheme.typography.titleSmall); content() }
    }
    @Composable fun Bullets(lines: List<String>) = lines.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    @Composable fun Checks(kind: String, lines: List<String>) = lines.forEachIndexed { index, line ->
        val key = leap.key(kind, index)
        val check = checks[key]
        Row(Modifier.fillMaxWidth().clickable { onToggle(key) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CheckMark(check != null, accent)
            Column(Modifier.weight(1f)) {
                Text(line, style = MaterialTheme.typography.bodyMedium)
                check?.let { Text(LocalDate.parse(it.date).format(dateFormat), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Section("Det kan du se") { Bullets(leap.seen) }
        Section("Lege at prøve") { Checks("play", leap.play) }
        Section("Det kan være svært") { Bullets(leap.hard) }
        Section("Færdigheder") { Checks("skill", leap.skills) }
    }
}

@Composable
private fun CheckMark(done: Boolean, accent: Color) {
    if (done) Icon(Icons.Filled.CheckCircle, "Klaret", tint = accent)
    else Icon(Icons.Outlined.RadioButtonUnchecked, "Ikke endnu", tint = accent.copy(alpha = 0.45f))
}

/** Weeks 0–80 as a strip: each spurt's unsettled period as a block, a line where the child is now with its week above, and the weeks below. */
@Composable
internal fun LeapStrip(weeks: Double, accent: Color, light: Color, legend: Boolean = false) {
    val end = 80.0
    val fraction = (weeks.coerceIn(0.0, end) / end).toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
            val labelWidth = 52.dp
            val x = (maxWidth * fraction - labelWidth / 2).coerceIn(0.dp, maxWidth - labelWidth)
            Text("uge ${weeks.toInt().coerceAtLeast(0)}", style = MaterialTheme.typography.labelSmall, color = accent,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.width(labelWidth).offset(x = x))
        }
        Canvas(Modifier.fillMaxWidth().height(22.dp).semantics { contentDescription = "Tidslinje over de ti spring, med barnets alder markeret" }) {
            val h = 12.dp.toPx()
            val top = (size.height - h) / 2
            drawRoundRect(light, Offset(0f, top), Size(size.width, h), CornerRadius(h / 2))
            leaps.forEach { leap ->
                val x1 = (leap.fussyFromWeek / end * size.width).toFloat()
                val x2 = (leap.calmFromWeek / end * size.width).toFloat()
                drawRoundRect(if (weeks >= leap.fussyFromWeek && weeks < leap.calmFromWeek) accent else accent.copy(alpha = 0.45f), Offset(x1, top), Size((x2 - x1).coerceAtLeast(3f), h), CornerRadius(3.dp.toPx()))
            }
            val x = fraction * size.width
            drawLine(Color(0xFF1C1B1F), Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
        }
        Row(Modifier.fillMaxWidth()) {
            // Four equal quarters from the due date to week 80, with the last label at the end.
            listOf("Termin", "20", "40", "60").forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)) }
            Text("80 uger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (legend) Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendMark("et spring") { drawRoundRect(accent.copy(alpha = 0.45f), cornerRadius = CornerRadius(2.dp.toPx())) }
            LegendMark("springet nu") { drawRoundRect(accent, cornerRadius = CornerRadius(2.dp.toPx())) }
            LegendMark("barnets alder") { drawLine(Color(0xFF1C1B1F), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2.dp.toPx()) }
        }
    }
}

@Composable
private fun LegendMark(label: String, draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(Modifier.width(12.dp).height(10.dp), onDraw = draw)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun dk.babyapp.domain.Milestone.isNow(months: Double) = months >= fromMonths && months <= toMonths

/** The keys of a spurt's games and skills, which can be ticked off. */
internal fun Leap.tickKeys() = play.indices.map { key("play", it) } + skills.indices.map { key("skill", it) }

/** Nu: one short card for each tab, all about the child's age right now, with a link into the tab. */
private fun nowItems(scope: LazyListScope, phase: LeapPhase, weeks: Double, months: Double, mine: Map<String, DevelopmentCheck>, accent: Color, toggle: (String) -> Unit, open: (DevelopmentTab) -> Unit) = with(scope) {
    val leap = (phase as? LeapPhase.InLeap)?.leap ?: (phase as LeapPhase.Calm).next
    if (leap != null) item(key = "now-leap") {
        NowCard(if (phase is LeapPhase.InLeap) "Spring ${leap.number} lige nu" else "Næste spring: ${leap.number}", "Se spring", { open(DevelopmentTab.Leaps) }) {
            Text(leap.title, style = MaterialTheme.typography.bodyMedium)
            leap.seen.take(2).forEach { DetailBullet(androidx.compose.ui.text.AnnotatedString(it)) }
            val keys = leap.tickKeys()
            Text("Lege og færdigheder: ${keys.count { mine[it] != null }} af ${keys.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    item(key = "now-milestones") {
        NowCard("Milepæle typisk nu", "Se alle", { open(DevelopmentTab.Milestones) }) {
            val now = milestones.filter { it.isNow(months) }
            if (now.isEmpty()) {
                val next = milestones.filter { it.fromMonths > months }.minByOrNull { it.fromMonths }
                Text(next?.let { "Ingen lige nu. Næste: ${it.title.lowercase()} fra ${it.window().substringBefore("–")} mdr." } ?: "Alle milepæle er passeret.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else now.forEach { m -> TickRow(m.title, mine["milestone-${m.key}"] != null, accent) { toggle("milestone-${m.key}") } }
        }
    }
    val psych = dk.babyapp.domain.psychNow(months)
    if (psych.isNotEmpty()) item(key = "now-psych") {
        NowCard("Psykologi nu", "Læs", { open(DevelopmentTab.Psychology) }) {
            psych.forEach { DetailBullet(androidx.compose.ui.text.AnnotatedString(it.title)) }
        }
    }
    item(key = "now-experiments") {
        NowCard("Eksperimenter at prøve", "Se alle", { open(DevelopmentTab.Experiments) }) {
            val todo = dk.babyapp.domain.experimentsNow(months).filter { mine[it.key] == null }.take(3)
            if (todo.isEmpty()) Text("I har lavet alle eksperimenter, der passer til alderen lige nu.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else todo.forEach { e -> TickRow("${dk.babyapp.domain.experimentOrder.getValue(e.number)}. ${e.title}", false, accent, experimentTick) { toggle(e.key) } }
        }
    }
}

/** One spurt: when it is, whether it is now, how many games and skills are ticked; the arrow opens the details. */
@Composable
private fun LeapCard(leap: Leap, id: String, start: LocalDate, weeks: Double, mine: Map<String, DevelopmentCheck>, accent: Color, toggle: (String) -> Unit) {
    val now = weeks >= leap.fussyFromWeek && weeks < leap.calmFromWeek
    val status = when {
        weeks >= leap.calmFromWeek -> "overstået"
        now -> "nu"
        else -> "kommer"
    }
    val keys = leap.tickKeys()
    val done = keys.count { mine[it] != null }
    val subtitle = listOfNotNull("Uge ${leap.fussyFromWeek.toInt()}–${leap.calmFromWeek.toInt()}", start.plusWeekFraction(leap.fussyFromWeek).format(dateFormat), status, "$done af ${keys.size} klaret".takeIf { done > 0 }).joinToString(" · ")
    DevItemCard(id, "Spring ${leap.number} · ${leap.title}", subtitle, accent, now, checked = null) {
        LeapDetails(leap, mine, accent, toggle, inCard = true)
    }
}

/** One milestone: the circle ticks it off with the date, the arrow opens what it means. */
@Composable
private fun MilestoneCard(milestone: dk.babyapp.domain.Milestone, id: String, months: Double, mine: Map<String, DevelopmentCheck>, accent: Color, toggle: (String) -> Unit) {
    val key = "milestone-${milestone.key}"
    val check = mine[key]
    val now = milestone.isNow(months)
    val subtitle = listOfNotNull(milestone.window(), "typisk nu".takeIf { now && check == null }, check?.let { "kunne den ${developmentDate(it.date)}" }).joinToString(" · ")
    DevItemCard(id, milestone.title, subtitle, accent, now && check == null, check != null, milestoneTick, { toggle(key) }) {
        Text(milestone.about, style = MaterialTheme.typography.bodyMedium)
        if (milestone.who) Text("Vinduet er WHO's: den alder, hvor 1 % til 99 % af raske børn når milepælen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

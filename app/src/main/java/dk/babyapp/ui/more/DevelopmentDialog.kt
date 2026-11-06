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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.domain.DevArea
import dk.babyapp.domain.DevState
import dk.babyapp.domain.Leap
import dk.babyapp.domain.LeapPhase
import dk.babyapp.domain.Milestone
import dk.babyapp.domain.developmentProgress
import dk.babyapp.domain.developmentStart
import dk.babyapp.domain.experimentOrder
import dk.babyapp.domain.experimentTimeline
import dk.babyapp.domain.isCurrent
import dk.babyapp.domain.leapPhase
import dk.babyapp.domain.leaps
import dk.babyapp.domain.milestones
import dk.babyapp.domain.monthsAndWeeks
import dk.babyapp.domain.monthsFromWeeks
import dk.babyapp.domain.plusWeekFraction
import dk.babyapp.domain.psychAgeBand
import dk.babyapp.domain.psychTimeline
import dk.babyapp.domain.state
import dk.babyapp.domain.tickKey
import dk.babyapp.domain.tickKeys
import dk.babyapp.domain.weeksFromDue
import dk.babyapp.domain.wholeMonthWindow
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
private val shortDateFormat = DateTimeFormatter.ofPattern("d. MMM", Locale.forLanguageTag("da"))

/** Said under Om in every tab instead of at the bottom of the page. */
internal const val developmentCaveat = "Alle børn udvikler sig i deres eget tempo, og aldrene er kun omtrentlige. Tal med sundhedsplejersken, hvis du er bekymret."

/**
 * Udvikling: an overview (Nu) and four tabs – the ten spurts counted from the due date, milestones, psychology and
 * experiments – that all look alike: Om, Lige nu, Fremtidige. Ticked items are kept per child.
 */
@Composable
internal fun DevelopmentDialog(child: ChildProfile, checks: List<DevelopmentCheck>, onUpdateChecks: (List<DevelopmentCheck>) -> Unit, onDismiss: () -> Unit) {
    val today = LocalDate.now()
    val start = developmentStart(child)
    val weeks = weeksFromDue(child, today)
    val mine = checks.filter { it.childId == child.id }.associateBy { it.key }
    val done = mine.keys
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
    val futureOpen = rememberSaveable { mutableStateOf(false) }
    val pastOpen = rememberSaveable { mutableStateOf(false) }
    val folds = DevFolds(futureOpen, pastOpen)
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
                        // Five tabs don't fit on one row, so they sit in two: the overview and the leaps and milestones, then psychology and experiments.
                        item(key = "tabs") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(DevelopmentTab.entries.take(3), DevelopmentTab.entries.drop(3)).forEach { row ->
                                    PeriodBar(row.map { it.name to it.label }, tab.name) {
                                        tab = DevelopmentTab.valueOf(it); futureOpen.value = false; pastOpen.value = false
                                    }
                                }
                            }
                        }
                        val months = monthsFromWeeks(weeks)
                        val week = weeks.toInt().coerceAtLeast(0)
                        val ageHeading = "Lige nu · ${monthsAndWeeks(months.coerceAtLeast(0.0)).substringBefore(" og")}"
                        when (tab) {
                            DevelopmentTab.Now -> nowItems(this, start, weeks, done, colors.accent, colors.container) { tab = it }
                            DevelopmentTab.Leaps -> {
                                // Between two spurts there is nothing under way, so Lige nu says when the next one begins.
                                val calm = leapPhase(weeks) as? LeapPhase.Calm
                                val note = calm?.let { phase -> phase.next?.let { "Rolig periode. Spring ${it.number} begynder om ca. ${Math.ceil(phase.weeksToNext ?: 0.0).toInt()} uger." } ?: "Alle ti spring er overstået." }
                                categoryItems(this, "leap", "Om spring", { LeapAbout(weeks, colors.accent, colors.container) },
                                    "Lige nu · uge $week", "", "Fremtidige spring",
                                    leaps.map { leap ->
                                        val state = leap.state(weeks, done)
                                        DevEntry("${leap.number}", state, psychAgeBand(monthsFromWeeks(leap.fussyFromWeek))) { LeapCard(leap, state, start, done, colors.accent, ::toggle) }
                                    }, folds, nowNote = note)
                            }
                            DevelopmentTab.Milestones -> categoryItems(this, "ms", "Om milepæle", { MilestoneAbout() }, ageHeading,
                                "Ingen milepæle typisk lige nu.", "Fremtidige milepæle",
                                milestones.sortedBy { it.fromMonths }.map { m ->
                                    val state = m.state(months, done)
                                    DevEntry(m.key, state, psychAgeBand(m.fromMonths)) { MilestoneCard(m, state, mine[m.tickKey], colors.accent) { toggle(m.tickKey) } }
                                }, folds)
                            DevelopmentTab.Psychology -> psychologyItems(this, months, ageHeading, done, colors.accent, ::toggle, folds)
                            DevelopmentTab.Experiments -> experimentItems(this, months, ageHeading, done, colors.accent, ::toggle, folds)
                        }
                    }
                    LocalTimerBar.current()
                }
            }
        }
    }
}

/** Om spring: what the spurts are, the strip of all ten with the child's week, and the caveat. */
@Composable
private fun LeapAbout(weeks: Double, accent: Color, light: Color) {
    Text("Springene er perioder, hvor mange børn en tid er mere urolige og derefter viser nye evner. Ugerne regnes fra terminsdatoen, også for børn født før eller efter termin.")
    Text("Åbn et spring for at se, hvad du kan se, lege at prøve, hvad der kan være svært, og færdigheder. Kryds lege og færdigheder af, når I har prøvet dem.")
    Text("De ti spring", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
    LeapStrip(weeks, accent, light, legend = true)
    Text("Springene er en model, og ugerne er kun omtrentlige. $developmentCaveat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun MilestoneAbout() {
    Text("Kryds en milepæl af, når barnet kan den. Vinduet viser, hvornår de fleste børn når den.")
    Text("De motoriske milepæle bruger WHO's vinduer: den alder, hvor 1 % til 99 % af raske børn når milepælen. De andre er typiske aldre.")
    Text("En milepæl, der står som \"fra tidligere\", er blot ikke krydset af endnu. Mange børn når den lidt senere, eller springer et trin over.")
    Text(developmentCaveat, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** What to look for, games, what can be hard and skills; games and skills can be ticked off. */
@Composable
private fun LeapDetails(leap: Leap, done: Set<String>, accent: Color, onToggle: (String) -> Unit) {
    @Composable fun Bullets(lines: List<String>) = lines.forEach { DetailBullet(AnnotatedString(it)) }
    @Composable fun Checks(kind: String, lines: List<String>) = lines.forEachIndexed { index, line ->
        val key = leap.key(kind, index)
        TickRow(line, key in done, accent) { onToggle(key) }
    }
    DetailHeading("Det kan du se"); Bullets(leap.seen)
    DetailHeading("Lege at prøve"); Checks("play", leap.play)
    DetailHeading("Det kan være svært"); Bullets(leap.hard)
    DetailHeading("Færdigheder"); Checks("skill", leap.skills)
}

/** A line that can be ticked off where it stands. */
@Composable
private fun TickRow(title: String, checked: Boolean, accent: Color, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TickIcon(checked, accent, title)
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
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

/** "Vender sig, Sidder uden støtte og 2 mere". */
private fun shortList(titles: List<String>, max: Int = 3): String {
    val shown = titles.take(max)
    val rest = titles.size - shown.size
    return if (rest > 0) shown.joinToString(", ") + " og $rest mere" else shown.dropLast(1).joinToString(", ").let { if (it.isEmpty()) shown.last() else "$it og ${shown.last()}" }
}

/**
 * Nu: the child's age with the strip of spurts, then one card per area in the same form: what is relevant at this
 * age, how much of it is done, and Se alle into the tab.
 */
private fun nowItems(scope: LazyListScope, start: LocalDate, weeks: Double, done: Set<String>, accent: Color, light: Color, open: (DevelopmentTab) -> Unit) = with(scope) {
    val months = monthsFromWeeks(weeks)
    val phase = leapPhase(weeks)
    item(key = "now-age") {
        WhiteCard {
            Text("Uge ${weeks.toInt().coerceAtLeast(0)} fra termin", style = MaterialTheme.typography.titleLarge)
            Text(monthsAndWeeks(months.coerceAtLeast(0.0)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LeapStrip(weeks, accent, light)
        }
    }
    item(key = "now-leaps") {
        val overdue = leaps.count { it.state(weeks, done) == DevState.Overdue }
        val lines = listOfNotNull(
            when (phase) {
                is LeapPhase.InLeap -> "Spring ${phase.leap.number} lige nu · ${phase.leap.title}. Urolig periode til ca. ${start.plusWeekFraction(phase.leap.calmFromWeek).format(shortDateFormat)}."
                is LeapPhase.Calm -> phase.next?.let { "Rolig periode. Næste spring: ${it.number} · ${it.title}, om ca. ${Math.ceil(phase.weeksToNext ?: 0.0).toInt()} uger." } ?: "Alle ti spring er overstået."
            },
            (if (overdue == 1) "1 tidligere spring har lege og færdigheder, der ikke er krydset af." else "$overdue tidligere spring har lege og færdigheder, der ikke er krydset af.").takeIf { overdue > 0 },
        )
        NowSummaryCard("Spring", lines, developmentProgress(DevArea.Leaps, weeks, done), "lege og færdigheder", accent, light) { open(DevelopmentTab.Leaps) }
    }
    item(key = "now-milestones") {
        val current = milestones.filter { it.state(months, done).isCurrent && it.tickKey !in done }.map { it.title }
        val next = milestones.filter { it.fromMonths > months }.minByOrNull { it.fromMonths }
        val lines = listOf(when {
            current.isNotEmpty() -> "Typisk nu: ${shortList(current)}."
            next != null -> "Ingen milepæle mangler lige nu. Næste: ${next.title.lowercase()} fra ca. ${next.wholeMonthWindow().substringBefore("–")} mdr."
            else -> "Alle milepæle er nået."
        })
        NowSummaryCard("Milepæle", lines, developmentProgress(DevArea.Milestones, weeks, done), "milepæle nået", accent, light) { open(DevelopmentTab.Milestones) }
    }
    item(key = "now-psych") {
        val unread = psychTimeline.filter { it.state(months, done).isCurrent && it.key !in done }.map { it.title }
        val lines = listOf(if (unread.isEmpty()) "Du har læst alt, der hører til alderen lige nu." else "At læse om nu: ${shortList(unread)}.")
        NowSummaryCard("Psykologi", lines, developmentProgress(DevArea.Psychology, weeks, done), "emner læst", accent, light) { open(DevelopmentTab.Psychology) }
    }
    item(key = "now-experiments") {
        val todo = experimentTimeline.filter { it.state(months, done).isCurrent && it.key !in done }.map { "${experimentOrder.getValue(it.number)}. ${it.title}" }
        val lines = listOf(if (todo.isEmpty()) "I har lavet alle eksperimenter, der passer til alderen lige nu." else "At prøve nu: ${shortList(todo)}.")
        NowSummaryCard("Eksperimenter", lines, developmentProgress(DevArea.Experiments, weeks, done), "eksperimenter lavet", accent, light) { open(DevelopmentTab.Experiments) }
    }
}

/** One spurt: its weeks, its status and how many games and skills are ticked; the popup has the dates and the details. */
@Composable
private fun LeapCard(leap: Leap, state: DevState, start: LocalDate, done: Set<String>, accent: Color, toggle: (String) -> Unit) {
    val keys = leap.tickKeys()
    val ticked = keys.count { it in done }
    val weeksText = "Uge ${leap.fussyFromWeek.toInt()}–${leap.calmFromWeek.toInt()}"
    val subtitle = listOfNotNull(weeksText, stateLabel(state, "alt klaret"), "$ticked af ${keys.size} klaret".takeIf { ticked > 0 && state != DevState.DoneNow && state != DevState.DonePast }).joinToString(" · ")
    val from = start.plusWeekFraction(leap.fussyFromWeek)
    val to = start.plusWeekFraction(leap.calmFromWeek)
    DevItemCard("Spring ${leap.number} · ${leap.title}", subtitle, accent, state, checked = null,
        popupSubtitle = "$weeksText · ca. ${from.format(shortDateFormat)} – ${to.format(dateFormat)}") {
        LeapDetails(leap, done, accent, toggle)
    }
}

/** One milestone: whole months on the card, the exact window and what it means in the popup. */
@Composable
private fun MilestoneCard(milestone: Milestone, state: DevState, check: DevelopmentCheck?, accent: Color, toggle: () -> Unit) {
    val subtitle = listOfNotNull(milestone.wholeMonthWindow(), stateLabel(state, "nået")).joinToString(" · ")
    DevItemCard(milestone.title, subtitle, accent, state, check != null, milestoneTick, toggle,
        popupSubtitle = "Typisk fra ${monthsAndWeeks(milestone.fromMonths)} til ${monthsAndWeeks(milestone.toMonths)}") {
        Text(milestone.about, style = MaterialTheme.typography.bodyMedium)
        if (milestone.who) Text("Vinduet er WHO's: ved ${monthsAndWeeks(milestone.fromMonths)} kan 1 % af raske børn det, ved ${monthsAndWeeks(milestone.toMonths)} kan 99 %.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        check?.let { Text("Krydset af ${developmentDate(it.date)}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

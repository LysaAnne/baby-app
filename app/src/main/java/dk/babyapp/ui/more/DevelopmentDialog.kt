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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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

private enum class DevelopmentTab(val label: String) { Now("Nu"), Timeline("Tidslinje"), Milestones("Milepæle") }

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
    fun toggle(key: String) = onUpdateChecks(mine[key]?.let { checks - it } ?: (checks + DevelopmentCheck(child.id, key, today.toString())))
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
                                LeapStrip(weeks, colors.accent, colors.container)
                            }
                        }
                        item { PeriodBar(DevelopmentTab.entries.map { it.name to it.label }, tab.name) { tab = DevelopmentTab.valueOf(it) } }
                        when (tab) {
                            DevelopmentTab.Now -> {
                                val leap = (phase as? LeapPhase.InLeap)?.leap ?: (phase as LeapPhase.Calm).next ?: leaps.last()
                                item { Text(if (phase is LeapPhase.InLeap) "Spring ${leap.number} lige nu" else "Næste spring: ${leap.number}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp)) }
                                item { LeapDetails(leap, mine, colors.accent, ::toggle) }
                            }
                            DevelopmentTab.Timeline -> items(leaps, key = { it.number }) { leap ->
                                var open by rememberSaveable(leap.number) { mutableStateOf(false) }
                                val status = when {
                                    weeks >= leap.calmFromWeek -> "Overstået"
                                    weeks >= leap.fussyFromWeek -> "Nu"
                                    else -> "Kommer"
                                }
                                WhiteCard {
                                    Row(Modifier.fillMaxWidth().clickable { open = !open }, verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text("Spring ${leap.number} · ${leap.title}", style = MaterialTheme.typography.titleSmall)
                                            Text("Uge ${leap.fussyFromWeek.toInt()}–${leap.calmFromWeek.toInt()} · ${start.plusWeekFraction(leap.fussyFromWeek).format(dateFormat)} · $status",
                                                style = MaterialTheme.typography.bodySmall, color = if (status == "Nu") colors.accent else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.Outlined.ExpandMore, if (open) "Skjul" else "Vis", modifier = Modifier.rotate(if (open) 180f else 0f))
                                    }
                                    if (open) LeapDetails(leap, mine, colors.accent, ::toggle, inCard = true)
                                }
                            }
                            DevelopmentTab.Milestones -> {
                                val months = weeks * 7 / 30.44
                                item { Text("Tryk på en milepæl, når barnet kan den. Vinduet viser, hvornår de fleste børn når den.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp)) }
                                items(milestones, key = { it.key }) { milestone ->
                                    val check = mine["milestone-${milestone.key}"]
                                    val now = months >= milestone.fromMonths && months <= milestone.toMonths
                                    Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                        Row(Modifier.clickable { toggle("milestone-${milestone.key}") }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            CheckMark(check != null, colors.accent)
                                            Column(Modifier.weight(1f)) {
                                                Text(milestone.title, style = MaterialTheme.typography.bodyLarge)
                                                Text(listOfNotNull(milestone.window(), "typisk nu".takeIf { now && check == null }, check?.let { "kunne den ${LocalDate.parse(it.date).format(dateFormat)}" }).joinToString(" · "),
                                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
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

/** Weeks 0–80 as a strip: each spurt's unsettled period as a block, and a line where the child is now. */
@Composable
private fun LeapStrip(weeks: Double, accent: Color, light: Color) {
    val end = 80.0
    Canvas(Modifier.fillMaxWidth().height(28.dp).semantics { contentDescription = "Tidslinje over de ti spring, med barnets alder markeret" }) {
        val h = 14.dp.toPx()
        val top = (size.height - h) / 2
        drawRoundRect(light, Offset(0f, top), Size(size.width, h), CornerRadius(h / 2))
        leaps.forEach { leap ->
            val x1 = (leap.fussyFromWeek / end * size.width).toFloat()
            val x2 = (leap.calmFromWeek / end * size.width).toFloat()
            drawRoundRect(if (weeks >= leap.fussyFromWeek && weeks < leap.calmFromWeek) accent else accent.copy(alpha = 0.45f), Offset(x1, top), Size((x2 - x1).coerceAtLeast(3f), h), CornerRadius(3.dp.toPx()))
        }
        val x = (weeks.coerceIn(0.0, end) / end * size.width).toFloat()
        drawLine(Color(0xFF1C1B1F), Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
    }
    Row(Modifier.fillMaxWidth()) {
        // Four equal quarters from the due date to week 80, with the last label at the end.
        listOf("Termin", "20 uger", "40 uger", "60 uger").forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)) }
        Text("80", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

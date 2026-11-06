package dk.babyapp.ui.more

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.domain.DevArea
import dk.babyapp.domain.LeapPhase
import dk.babyapp.domain.developmentProgress
import dk.babyapp.domain.leapPhase
import dk.babyapp.domain.plusWeekFraction
import dk.babyapp.domain.weeksFromDue
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import dk.babyapp.ui.tracking.CollapsibleCard
import dk.babyapp.ui.tracking.FormDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The development card on I dag: the spurt the child is in (or the next one), the strip of spurts with the child's
 * week, and small counts for milestones, psychology and experiments at this age. Tap anywhere to open Udvikling.
 */
@Composable
internal fun DevelopmentStripCard(child: ChildProfile, checks: List<DevelopmentCheck>, collapsed: Boolean, onCollapse: (Boolean) -> Unit, onOpen: () -> Unit) {
    val colors = MaterialTheme.babyColors.category(CareCategory.Measurement)
    val weeks = weeksFromDue(child)
    CollapsibleCard(Icons.Outlined.TrendingUp, "Udvikling", collapsed, onCollapse) {
        Column(Modifier.fillMaxWidth().clickable(onClickLabel = "Åbn Udvikling", onClick = onOpen), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (weeks == null) Text("Angiv terminsdatoen for at se barnets udvikling.", style = MaterialTheme.typography.bodySmall)
            else {
                val start = dk.babyapp.domain.developmentStart(child)
                val week = weeks.toInt().coerceAtLeast(0)
                when (val phase = leapPhase(weeks)) {
                    is LeapPhase.InLeap -> {
                        Text("Spring ${phase.leap.number} · ${phase.leap.title}", style = MaterialTheme.typography.titleSmall)
                        Text("Uge $week · urolig periode" + (start?.let { " til ca. ${it.plusWeekFraction(phase.leap.calmFromWeek).format(shortDate)}" } ?: ""),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is LeapPhase.Calm -> {
                        Text(phase.next?.let { "Næste: spring ${it.number} · ${it.title}" } ?: "Alle ti spring er overstået", style = MaterialTheme.typography.titleSmall)
                        Text("Uge $week · rolig periode" + (phase.next?.let { " · om ca. ${Math.ceil(phase.weeksToNext ?: 0.0).toInt()} uger" } ?: ""),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                LeapStrip(weeks, colors.accent, colors.container)
                val mine = checks.filter { it.childId == child.id }.map { it.key }.toSet()
                // Done out of all for what belongs to the age now, for each area; an area that is all done is left out.
                val counts = listOf(DevArea.Leaps to "Spring", DevArea.Milestones to "Milepæle", DevArea.Psychology to "Psykologi", DevArea.Experiments to "Eksperimenter")
                    .map { (area, label) -> label to developmentProgress(area, weeks, mine) }
                    .filter { (_, progress) -> progress.total > 0 && !progress.complete }
                if (counts.isNotEmpty()) FlowRow(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    counts.forEach { (label, progress) -> Chip("$label ${progress.done}/${progress.total}", colors.container, colors.accent) }
                }
            }
        }
    }
}

private val shortDate = DateTimeFormatter.ofPattern("d. MMM", Locale.forLanguageTag("da"))

@Composable
private fun Chip(text: String, background: androidx.compose.ui.graphics.Color, accent: androidx.compose.ui.graphics.Color) {
    androidx.compose.material3.Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(50), color = background) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = accent, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}

/**
 * Opens Udvikling, but asks for the due date first when the child has none: the spurts are counted from it.
 * The due date is saved on the child's profile.
 */
@Composable
internal fun DevelopmentEntry(child: ChildProfile, checks: List<DevelopmentCheck>, onUpdateChecks: (List<DevelopmentCheck>) -> Unit, onUpdateChild: (ChildProfile) -> Unit, onDismiss: () -> Unit) {
    if (child.dueDate == null) DueDatePrompt(child, onDismiss) { due -> onUpdateChild(child.copy(dueDate = due)) }
    else DevelopmentDialog(child, checks, onUpdateChecks, onDismiss)
}

@Composable
private fun DueDatePrompt(child: ChildProfile, onDismiss: () -> Unit, onSave: (LocalDate) -> Unit) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(child.birthDate ?: LocalDate.now()) }
    FormDialog(category = CareCategory.Measurement, onDismissRequest = onDismiss, title = { Text("Terminsdato") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Udviklingen regnes fra terminsdatoen, også hvis ${child.name} er født før eller efter termin. Hvilken dato var termin?")
            OutlinedButton(onClick = {
                DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CalendarToday, null); Text(" " + date.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.forLanguageTag("da"))))
            }
        }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } }, confirmButton = { Button(onClick = { onSave(date) }) { Text("Gem") } })
}

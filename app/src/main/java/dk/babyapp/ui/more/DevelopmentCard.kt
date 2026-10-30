package dk.babyapp.ui.more

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import dk.babyapp.domain.LeapPhase
import dk.babyapp.domain.leapPhase
import dk.babyapp.domain.weeksFromDue
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import dk.babyapp.ui.tracking.CollapsibleCard
import dk.babyapp.ui.tracking.FormDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The development strip on I dag: where the child is in the spurts. Tap to open Udvikling on Nu. */
@Composable
internal fun DevelopmentStripCard(child: ChildProfile, collapsed: Boolean, onCollapse: (Boolean) -> Unit, onOpen: () -> Unit) {
    val colors = MaterialTheme.babyColors.category(CareCategory.Measurement)
    val weeks = weeksFromDue(child)
    CollapsibleCard(Icons.Outlined.TrendingUp, "Udvikling", collapsed, onCollapse, onOpen = onOpen) {
        Column(Modifier.fillMaxWidth().clickable(onClickLabel = "Åbn Udvikling", onClick = onOpen), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (weeks == null) Text("Angiv terminsdatoen for at se barnets udvikling.", style = MaterialTheme.typography.bodySmall)
            else {
                Text("Uge ${weeks.toInt().coerceAtLeast(0)} · " + when (val phase = leapPhase(weeks)) {
                    is LeapPhase.InLeap -> "spring ${phase.leap.number}: ${phase.leap.title}"
                    is LeapPhase.Calm -> phase.next?.let { "rolig periode · spring ${it.number} om ca. ${Math.ceil(phase.weeksToNext ?: 0.0).toInt()} uger" } ?: "alle spring er overstået"
                }, style = MaterialTheme.typography.bodyMedium)
                LeapStrip(weeks, colors.accent, colors.container)
            }
        }
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

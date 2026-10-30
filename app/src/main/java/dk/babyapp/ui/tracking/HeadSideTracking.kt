package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*
import java.time.LocalDate

/** The Liggeside quick button's window: Venstre or Højre with today's count; a tap registers it. */
@Composable
internal fun HeadSidePickDialog(events: List<CareEventEntity>, childId: String, onSave: (CareEventEntity) -> Unit, onDismiss: () -> Unit) {
    val counts = headSideTotals(eventsForDay(events.filter { it.childId == childId }, LocalDate.now()))
    QuickChoiceDialog("Liggeside", dk.babyapp.ui.theme.CareCategory.Sleep, HeadSide.entries.map { side ->
        QuickChoice("${side.displayLabel()} · ${if (side == HeadSide.Left) counts.left else counts.right}", "Registrer hovedside ${side.displayLabel().lowercase()}") { onSave(headSideObservation(childId, side)) }
    }, onDismiss)
}

@Composable
internal fun HeadSideEditor(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var time by remember(event.id) { mutableLongStateOf(event.startedAt) }
    var side by remember(event.id) { mutableStateOf(event.headSide) }
    var notes by remember(event.id) { mutableStateOf(event.notes) }
    var error by remember { mutableStateOf<String?>(null) }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Sleep, onDismissRequest = onDismiss, title = { Text("Rediger favoritside") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateTimeFields(time) { time = it; error = null }
            SelectionDropdown("Hovedside", side.displayLabel(), HeadSide.entries.map { it to it.displayLabel() }) { side = it }
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { Button(enabled = side != null, onClick = {
        if (time > System.currentTimeMillis()) error = "Tidspunktet kan ikke ligge i fremtiden."
        else onSave(event.copy(startedAt = time, endedAt = time, headSide = side, notes = notes, runningSince = null))
    }) { Text("Gem") } }, dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuller") } })
}

package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*
import java.time.LocalDate

@Composable
internal fun HeadSideQuickButtons(events: List<CareEventEntity>, childId: String?, onSave: (CareEventEntity) -> Unit) {
    val counts = headSideTotals(eventsForDay(events.filter { it.childId == childId }, LocalDate.now()))
    Text("Favoritside", style = MaterialTheme.typography.titleSmall)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HeadSide.entries.forEach { side ->
            FilledTonalButton(modifier = Modifier.weight(1f).semantics { contentDescription = "Registrer hovedside ${side.displayLabel().lowercase()}" }, enabled = childId != null, onClick = {
                childId?.let { onSave(headSideObservation(it, side)) }
            }) { Text("${side.displayLabel()} · ${if (side == HeadSide.Left) counts.left else counts.right}") }
        }
    }
}

@Composable
internal fun HeadSideEditor(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var time by remember(event.id) { mutableLongStateOf(event.startedAt) }
    var side by remember(event.id) { mutableStateOf(event.headSide) }
    var notes by remember(event.id) { mutableStateOf(event.notes) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rediger favoritside") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateTimeFields(time) { time = it; error = null }
            SelectionDropdown("Hovedside", side.displayLabel(), HeadSide.entries.map { it to it.displayLabel() }) { side = it }
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(enabled = side != null, onClick = {
        if (time > System.currentTimeMillis()) error = "Tidspunktet kan ikke ligge i fremtiden."
        else onSave(event.copy(startedAt = time, endedAt = time, headSide = side, notes = notes, runningSince = null))
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

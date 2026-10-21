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

@Composable
internal fun HeadSideQuickButtons(events: List<CareEventEntity>, childId: String?, onOpenStatistics: (() -> Unit)? = null, onSave: (CareEventEntity) -> Unit) {
    val counts = headSideTotals(eventsForDay(events.filter { it.childId == childId }, LocalDate.now()))
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = if (onOpenStatistics != null) 48.dp else 0.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Favoritside", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            if (onOpenStatistics != null) IconButton(onClick = onOpenStatistics, enabled = childId != null) {
                Icon(Icons.Outlined.AutoGraph, contentDescription = "Åbn favoritside")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HeadSide.entries.forEach { side ->
                QuickButton("${side.displayLabel()} · ${if (side == HeadSide.Left) counts.left else counts.right}", Modifier.weight(1f).semantics { contentDescription = "Registrer hovedside ${side.displayLabel().lowercase()}" }, childId != null) {
                    childId?.let { onSave(headSideObservation(it, side)) }
                }
            }
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

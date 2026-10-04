package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*

@Composable
internal fun NursingEditor(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var intervals by remember(event.id) { mutableStateOf(event.nursingIntervals()) }
    var notes by remember(event.id) { mutableStateOf(event.notes) }
    var issue by remember(event.id) { mutableStateOf(event.breastfeedingIssue) }
    var error by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rediger amning", style = MaterialTheme.typography.headlineSmall)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ret tid og brystside for hvert interval. Pauser tæller ikke med. Et igangværende interval fortsætter, mens du redigerer.")
                    intervals.forEachIndexed { index, interval ->
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Interval ${index + 1}${if (interval.end == null) " · kører" else ""}", style = MaterialTheme.typography.titleMedium)
                            Text("Start")
                            DateTimeFields(interval.start) { time -> intervals = intervals.toMutableList().apply { this[index] = interval.copy(start = time) }; error = null }
                            if (interval.end != null) { Text("Slut"); DateTimeFields(interval.end) { time -> intervals = intervals.toMutableList().apply { this[index] = interval.copy(end = time) }; error = null } }
                            SelectionDropdown("Brystside", interval.side?.let { if (it == BreastSide.Left) "Venstre" else "Højre" } ?: "Side ikke registreret", BreastSide.entries.map { it to if (it == BreastSide.Left) "Venstre" else "Højre" }) { side -> intervals = intervals.toMutableList().apply { this[index] = interval.copy(side = side) }; error = null }
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Checkbox(interval.nippleShield, { enabled -> intervals = intervals.toMutableList().apply { this[index] = interval.copy(nippleShield = enabled) } }, modifier = Modifier.semantics { contentDescription = "Ammebrik" }); Text("Ammebrik") }
                            TextButton(onClick = { intervals = intervals.filterIndexed { i, _ -> i != index }; error = null }) { Text("Slet interval", color = MaterialTheme.colorScheme.error) }
                        } }
                    }
                    if (intervals.isEmpty()) Text("Alle intervaller er fjernet. Gem giver 0 minutters amning. Du kan annullere for at beholde dem.")
                    SelectionDropdown("Gener", issue?.displayLabel() ?: "Ingen angivet", listOf<BreastfeedingIssue?>(null).map { it to "Ingen angivet" } + BreastfeedingIssue.entries.map { it as BreastfeedingIssue? to it.displayLabel() }) { issue = it }
                    OutlinedTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Noter") })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("Annuller") }
                    Button(onClick = {
                        runCatching { event.editNursingIntervals(intervals, System.currentTimeMillis()).copy(notes = notes, breastfeedingIssue = issue) }
                            .onSuccess(onSave).onFailure { error = it.message ?: "Kontrollér intervallerne." }
                    }) { Text("Gem") }
                }
            }
        }
    }
}

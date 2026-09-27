package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.babyapp.data.preferences.orderedQuickActionCategories
import dk.babyapp.data.preferences.QuickAction
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType

@Composable
internal fun DiaperObservationField(value: String, onChange: (String) -> Unit) {
    val options = listOf("", "Rød numse", "Irriteret hud", "Udslæt", "Sår på huden", "Andet")
    SelectionDropdown("Observation", value.ifBlank { "Ingen observation" }, (options + listOfNotNull(value.takeIf { it.isNotBlank() && it !in options })).map { it to it.ifBlank { "Ingen observation" } }, onChange)
    if (value == "Andet") Text("Beskriv observationen i Noter.", style = MaterialTheme.typography.bodySmall)
}

@Composable
internal fun SelectionStartPumpDialog(onDismiss: () -> Unit, onStart: (String) -> Unit) {
    var method by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Start pumpning") }, text = {
        SelectionDropdown("Metode (valgfrit)", method.ifBlank { "Ikke angivet" }, listOf("", "Hånd", "Maskine", "Manuel pumpe").map { it to it.ifBlank { "Ikke angivet" } }) { method = it }
    }, confirmButton = { Button(onClick = { onStart(method) }) { Text("Start") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun AllQuickActionsDialog(hidden: Set<String>, order: List<String>, onDismiss: () -> Unit, onSave: (Set<String>, List<String>) -> Unit) {
    var selectedHidden by remember { mutableStateOf(hidden) }
    var selectedOrder by remember { mutableStateOf(orderedQuickActionCategories(order)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilpas hurtig registrering") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedOrder.forEachIndexed { index, category ->
                val actions = QuickAction.entries.filter { it.category == category }
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    Text(category, style = MaterialTheme.typography.titleMedium)
                    Row {
                        TextButton(enabled = index > 0, onClick = { selectedOrder = selectedOrder.toMutableList().apply { add(index - 1, removeAt(index)) } }) { Text("Flyt op") }
                        TextButton(enabled = index < selectedOrder.lastIndex, onClick = { selectedOrder = selectedOrder.toMutableList().apply { add(index + 1, removeAt(index)) } }) { Text("Flyt ned") }
                    }
                    actions.forEach { action -> Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(action.name !in selectedHidden, { visible -> selectedHidden = if (visible) selectedHidden - action.name else selectedHidden + action.name })
                        Text(action.label)
                    } }
                } }
            }
        }
    }, confirmButton = { Button(onClick = { onSave(selectedHidden, selectedOrder) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun SolidFoodDialog(childId: String, existing: CareEventEntity?, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    val now = remember { System.currentTimeMillis() }
    var draft by remember { mutableStateOf(existing ?: CareEventEntity(childId = childId, type = CareEventType.SolidFood, startedAt = now, endedAt = now)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (existing == null) "Registrér fast føde" else "Rediger fast føde") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateAndOptionalTimeFields(draft.startedAt, draft.timeSpecified, { draft = draft.copy(startedAt = it, endedAt = it) }, { draft = draft.copy(timeSpecified = it) })
            OutlinedTextField(draft.foodName, { draft = draft.copy(foodName = it) }, label = { Text("Mad / ingredienser *") })
            SelectionDropdown("Konsistens", draft.foodTexture.ifBlank { "Vælg" }, listOf("Smagsprøve", "Grød", "Fin mos", "Grov mos", "Bløde stykker / fingermad", "Familiemad", "Andet").map { it to it }) { draft = draft.copy(foodTexture = it) }
            OutlinedTextField(draft.foodAmount, { draft = draft.copy(foodAmount = it) }, label = { Text("Spist mængde, fx 3 tsk eller 40 g") })
            SelectionDropdown("Oplevelse / reaktion", draft.foodReaction.ifBlank { "Ikke angivet" }, listOf("Ikke angivet", "Spiste med appetit", "Smagte lidt", "Afviste maden", "Gylp / opkast", "Hudreaktion", "Andet").map { it to it }) { draft = draft.copy(foodReaction = it) }
            OutlinedTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Noter, fx nye ingredienser") })
        }
    }, confirmButton = { Button(enabled = draft.foodName.isNotBlank(), onClick = { onSave(draft.copy(foodName = draft.foodName.trim())) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

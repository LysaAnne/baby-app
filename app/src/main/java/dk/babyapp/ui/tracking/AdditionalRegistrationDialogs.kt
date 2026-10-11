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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Add
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
internal fun PumpingDialog(childId: String, initialMethod: String, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var method by remember { mutableStateOf(initialMethod) }
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Registrer pumpning") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateTimeFields(time) { time = it }
            SelectionDropdown("Pumpetype", method.ifBlank { "Ikke angivet" }, listOf("", "Hånd", "Maskine", "Manuel pumpe").map { it to it.ifBlank { "Ikke angivet" } }) { method = it }
            RegistrationTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Pumpet (ml)") })
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
        }
    }, confirmButton = { Button(enabled = amount.isBlank() || amount.toIntOrNull() != null, onClick = {
        onSave(CareEventEntity(childId = childId, type = CareEventType.Pumping, startedAt = time, endedAt = time, pumpedAmountMl = amount.toIntOrNull(), pumpingMethod = method, notes = notes))
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun AllQuickActionsDialog(hidden: Set<String>, order: List<String>, onDismiss: () -> Unit, onSave: (Set<String>, List<String>) -> Unit) {
    var selectedHidden by remember { mutableStateOf(hidden) }
    var selectedOrder by remember { mutableStateOf(orderedQuickActionCategories(order)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilpas hurtig registrering") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            selectedOrder.forEachIndexed { index, category ->
                val actions = QuickAction.entries.filter { it.category == category }
                val sectionHidden = actions.all { it.name in selectedHidden }
                OutlinedCard(Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = if (sectionHidden) androidx.compose.ui.graphics.Color(0xFFE1E1E1) else MaterialTheme.colorScheme.surface, contentColor = if (sectionHidden) androidx.compose.ui.graphics.Color(0xFF737373) else MaterialTheme.colorScheme.onSurface)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(category, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CustomizationIconButton("Flyt $category op", Icons.Outlined.ArrowUpward, index > 0) { selectedOrder = selectedOrder.toMutableList().apply { add(index - 1, removeAt(index)) } }
                        CustomizationIconButton("Flyt $category ned", Icons.Outlined.ArrowDownward, index < selectedOrder.lastIndex) { selectedOrder = selectedOrder.toMutableList().apply { add(index + 1, removeAt(index)) } }
                        FilledTonalButton(onClick = { selectedHidden = if (sectionHidden) selectedHidden - actions.map { it.name }.toSet() else selectedHidden + actions.map { it.name } }) {
                            Icon(if (sectionHidden) Icons.Outlined.Add else Icons.Outlined.Close, null)
                            Text(if (sectionHidden) "Vis" else "Skjul")
                        }
                    }
                    if (!sectionHidden) actions.forEach { action ->
                        HorizontalDivider()
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(action.label, Modifier.weight(1f))
                            val visible = action.name !in selectedHidden
                            CustomizationIconButton(if (visible) "Fjern ${action.label}" else "Tilføj ${action.label}", if (visible) Icons.Outlined.Close else Icons.Outlined.Add) {
                                selectedHidden = if (visible) selectedHidden + action.name else selectedHidden - action.name
                            }
                        }
                    }
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
            RegistrationTextField(draft.foodName, { draft = draft.copy(foodName = it) }, label = { Text("Mad / ingredienser *") })
            SelectionDropdown("Konsistens", draft.foodTexture.ifBlank { "Vælg" }, listOf("Smagsprøve", "Grød", "Fin mos", "Grov mos", "Bløde stykker / fingermad", "Familiemad", "Andet").map { it to it }) { draft = draft.copy(foodTexture = it) }
            RegistrationTextField(draft.foodAmount, { draft = draft.copy(foodAmount = it) }, label = { Text("Spist mængde, fx 3 tsk eller 40 g") })
            SelectionDropdown("Oplevelse / reaktion", draft.foodReaction.ifBlank { "Ikke angivet" }, listOf("Ikke angivet", "Spiste med appetit", "Smagte lidt", "Afviste maden", "Gylp / opkast", "Hudreaktion", "Andet").map { it to it }) { draft = draft.copy(foodReaction = it) }
            RegistrationTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Noter, fx nye ingredienser") })
        }
    }, confirmButton = { Button(enabled = draft.foodName.isNotBlank(), onClick = { onSave(draft.copy(foodName = draft.foodName.trim())) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun BottleRatioField(value: String, onChange: (String) -> Unit) {
    val amounts = remember { Regex("^(\\d+) ml \\+ (\\d+) ml").find(value) }
    var breastMilk by remember { mutableStateOf(amounts?.groupValues?.get(1).orEmpty()) }
    var formula by remember { mutableStateOf(amounts?.groupValues?.get(2).orEmpty()) }
    fun update() {
        val milk = breastMilk.toIntOrNull() ?: 0
        val replacement = formula.toIntOrNull() ?: 0
        var a = milk; var b = replacement
        while (b != 0) { val remainder = a % b; a = b; b = remainder }
        onChange(if (a == 0) "" else "$milk ml + $replacement ml (${milk / a}:${replacement / a})")
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RegistrationTextField(breastMilk, { breastMilk = it.filter(Char::isDigit).take(6); update() }, Modifier.weight(1f), label = { Text("Modermælk (ml)") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        RegistrationTextField(formula, { formula = it.filter(Char::isDigit).take(6); update() }, Modifier.weight(1f), label = { Text("Erstatning (ml)") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
    }
    if (value.isNotBlank()) Text("Modermælk : erstatning · $value", style = MaterialTheme.typography.bodySmall)
}

@Composable
internal fun CustomizationIconButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, enabled = enabled) { Icon(icon, contentDescription = label) }
}

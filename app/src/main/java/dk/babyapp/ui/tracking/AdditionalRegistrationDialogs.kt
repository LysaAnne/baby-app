package dk.babyapp.ui.tracking

import dk.babyapp.ui.theme.careCategory
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import dk.babyapp.ui.theme.babyColors
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
    val options = listOf("Rød numse", "Irriteret hud", "Udslæt", "Sår på huden", "Andet")
    ChoiceChips((options + listOfNotNull(value.takeIf { it.isNotBlank() && it !in options })).map { it to it }, value.ifBlank { null }, allowNone = true, label = "Observation") { onChange(it.orEmpty()) }
    if (value == "Andet") Text("Beskriv observationen i Noter.", style = MaterialTheme.typography.bodySmall)
}

@Composable
internal fun PumpingDialog(childId: String, initialMethod: String, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var method by remember { mutableStateOf(initialMethod) }
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Feeding, onDismissRequest = onDismiss, title = { Text("Pumpning") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AmountField(amount, { amount = it }, "Pumpet (ml)")
            PumpingMethodChips(method) { method = it }
            MoreDetails("tidspunkt, noter") {
                DateTimeFields(time) { time = it }
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        }
    }, confirmButton = { Button(enabled = amount.isBlank() || amount.toIntOrNull() != null, onClick = {
        onSave(CareEventEntity(childId = childId, type = CareEventType.Pumping, startedAt = time, endedAt = time, pumpedAmountMl = amount.toIntOrNull(), pumpingMethod = method, notes = notes))
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

/** The buttons a quick-registration category can show; nap and night are one Søvn button. */
private fun buttonsOf(category: dk.babyapp.data.preferences.QuickActionCategory) =
    QuickAction.entries.filter { it.category == category && it != QuickAction.HealthNote && it != QuickAction.Night }

private fun QuickAction.names() = if (this == QuickAction.Nap) setOf(QuickAction.Nap.name, QuickAction.Night.name) else setOf(name)
private fun QuickAction.shownLabel() = if (this == QuickAction.Nap) "Søvn" else label

@Composable
internal fun AllQuickActionsDialog(hidden: Set<String>, order: List<dk.babyapp.data.preferences.QuickActionCategory>, onDismiss: () -> Unit, simple: Boolean = false, onSimpleChange: (Boolean) -> Unit = {}, onSave: (Set<String>, List<dk.babyapp.data.preferences.QuickActionCategory>) -> Unit) {
    var selectedHidden by remember { mutableStateOf(hidden) }
    var selectedOrder by remember { mutableStateOf(orderedQuickActionCategories(order)) }
    var simpleView by remember { mutableStateOf(simple) }
    var open by remember { mutableStateOf<dk.babyapp.data.preferences.QuickActionCategory?>(null) }
    FormDialog(onDismissRequest = onDismiss, title = { Text("Tilpas hurtig registrering") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Simpel visning", style = MaterialTheme.typography.titleSmall)
                    Text("Ikoner og knapper uden overskrifter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(simpleView, { simpleView = it })
            }
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Text("Slå kort til og fra, og flyt dem. Fold et kort ud for at vælge dets knapper.", style = MaterialTheme.typography.bodyMedium)
            selectedOrder.forEachIndexed { index, category ->
                val actions = buttonsOf(category)
                val sectionShown = actions.any { it.name !in selectedHidden }
                val (up, down) = selectedOrder.moveCallbacks(index) { selectedOrder = it }
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        CustomizeRow(category.label, sectionShown, { on -> selectedHidden = if (on) selectedHidden - actions.flatMap { it.names() }.toSet() else selectedHidden + actions.flatMap { it.names() } }, up, down)
                    }
                    // The card's buttons are folded away until asked for.
                    if (actions.size > 1) IconButton(onClick = { open = if (open == category) null else category }) {
                        Icon(if (open == category) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (open == category) "Skjul knapper for ${category.label}" else "Vis knapper for ${category.label}")
                    }
                }
                if (open == category && sectionShown) actions.forEach { action ->
                    CustomizeRow(action.shownLabel(), action.name !in selectedHidden, { on -> selectedHidden = if (on) selectedHidden - action.names() else selectedHidden + action.names() }, indent = true)
                }
            }
        }
    }, confirmButton = { Button(onClick = { if (simpleView != simple) onSimpleChange(simpleView); onSave(selectedHidden, selectedOrder) }) { Text("Gem") } }, dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuller") } })
}

/** The settings icon on a single quick card: just that card's buttons, on or off. */
@Composable
internal fun CardButtonsDialog(category: dk.babyapp.data.preferences.QuickActionCategory, hidden: Set<String>, onDismiss: () -> Unit, onSave: (Set<String>) -> Unit) {
    var selectedHidden by remember { mutableStateOf(hidden) }
    FormDialog(onDismissRequest = onDismiss, category = category.careCategory, title = { Text("Tilpas ${category.label.lowercase()}") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Vælg de knapper, kortet viser.", style = MaterialTheme.typography.bodyMedium)
            buttonsOf(category).forEach { action ->
                CustomizeRow(action.shownLabel(), action.name !in selectedHidden, { on -> selectedHidden = if (on) selectedHidden - action.names() else selectedHidden + action.names() })
            }
        }
    }, confirmButton = { Button(onClick = { onSave(selectedHidden) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun SolidFoodDialog(childId: String, existing: CareEventEntity?, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    val now = remember { System.currentTimeMillis() }
    var draft by remember { mutableStateOf(existing ?: CareEventEntity(childId = childId, type = CareEventType.SolidFood, startedAt = now, endedAt = now)) }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.SolidFood, onDismissRequest = onDismiss, title = { Text(if (existing == null) "Fast føde" else "Rediger fast føde") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RegistrationTextField(draft.foodName, { draft = draft.copy(foodName = it) }, label = { Text("Mad / ingredienser *") })
            val textures = listOf("Smagsprøve", "Grød", "Fin mos", "Grov mos", "Bløde stykker / fingermad", "Familiemad", "Andet")
            ChoiceChips((textures + listOfNotNull(draft.foodTexture.takeIf { it.isNotBlank() && it !in textures })).map { it to it }, draft.foodTexture.ifBlank { null }, allowNone = true, label = "Konsistens") { draft = draft.copy(foodTexture = it.orEmpty()) }
            DateAndOptionalTimeFields(draft.startedAt, draft.timeSpecified, { draft = draft.copy(startedAt = it, endedAt = it) }, { draft = draft.copy(timeSpecified = it) })
            MoreDetails("mængde, reaktion, noter", listOf(draft.foodAmount, draft.foodReaction, draft.notes).any(String::isNotBlank)) {
                RegistrationTextField(draft.foodAmount, { draft = draft.copy(foodAmount = it) }, label = { Text("Spist mængde, fx 3 tsk eller 40 g") })
                val reactions = listOf("Spiste med appetit", "Smagte lidt", "Afviste maden", "Gylp / opkast", "Hudreaktion", "Andet")
                ChoiceChips((reactions + listOfNotNull(draft.foodReaction.takeIf { it.isNotBlank() && it != "Ikke angivet" && it !in reactions })).map { it to it }, draft.foodReaction.takeIf { it.isNotBlank() && it != "Ikke angivet" }, allowNone = true, label = "Oplevelse / reaktion") { draft = draft.copy(foodReaction = it.orEmpty()) }
                RegistrationTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Noter, fx nye ingredienser") })
            }
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

/** Pumping method as chips; "" means not given. */
@Composable
internal fun PumpingMethodChips(value: String, onChange: (String) -> Unit) {
    ChoiceChips(listOf("Hånd", "Maskine", "Manuel pumpe").map { it to it }, value.ifBlank { null }, allowNone = true, label = "Pumpetype") { onChange(it.orEmpty()) }
}

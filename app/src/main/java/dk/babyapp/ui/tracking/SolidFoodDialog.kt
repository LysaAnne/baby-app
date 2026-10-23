package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.domain.FirstFood
import dk.babyapp.domain.hundredFoods
import dk.babyapp.domain.hundredFoodsProgress
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors

/**
 * Fast føde with the 100 før 1 list built in: type what the child ate and pick from the list, or add your own.
 * A meal can hold several foods; they are saved as one comma-separated food name, so they count on the list.
 * [events] give the suggestions and counts; [onOpenList] opens the whole list.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SolidFoodDialog(
    childId: String,
    existing: CareEventEntity?,
    onDismiss: () -> Unit,
    events: List<CareEventEntity> = emptyList(),
    onOpenList: (() -> Unit)? = null,
    onSave: (CareEventEntity) -> Unit,
) {
    val now = remember { System.currentTimeMillis() }
    var draft by remember { mutableStateOf(existing ?: CareEventEntity(childId = childId, type = CareEventType.SolidFood, startedAt = now, endedAt = now)) }
    var foods by remember { mutableStateOf(existing?.foodName.orEmpty().split(',').map(String::trim).filter(String::isNotBlank)) }
    // An unchanged food name is saved exactly as it was written.
    var foodsChanged by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val progress = remember(events, childId) { hundredFoodsProgress(events.filter { it.id != existing?.id }, childId) }
    val recent = remember(events, childId) {
        events.filter { it.childId == childId && it.type == CareEventType.SolidFood && it.deletedAt == null && !it.isDraft }
            .sortedByDescending { it.startedAt }.flatMap { it.foodName.split(',').map(String::trim) }.filter(String::isNotBlank).distinct().take(6)
    }
    val accent = MaterialTheme.babyColors.category(CareCategory.SolidFood).accent
    fun add(food: String) { if (foods.none { it.equals(food, ignoreCase = true) }) { foods = foods + food; foodsChanged = true }; query = "" }
    fun label(food: FirstFood) = progress[food]?.let { "${food.name} · ${it.count}×" } ?: food.name
    fun foodName() = (if (foodsChanged) foods.joinToString(", ") else existing?.foodName?.trim().orEmpty().ifBlank { foods.joinToString(", ") })
        .let { name -> listOf(name, query.trim()).filter(String::isNotBlank).joinToString(", ") }

    FormDialog(category = CareCategory.SolidFood, onDismissRequest = onDismiss, title = { Text(if (existing == null) "Fast føde" else "Rediger fast føde") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (foods.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                foods.forEach { food ->
                    InputChip(selected = true, onClick = { foods = foods - food; foodsChanged = true }, label = { Text(food) },
                        trailingIcon = { Icon(Icons.Outlined.Close, "Fjern $food", Modifier.size(16.dp)) })
                }
            }
            RegistrationTextField(query, { query = it }, label = { Text(if (foods.isEmpty()) "Hvad spiste barnet?" else "Tilføj mere") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if (query.isNotBlank()) add(query.trim()) }))
            val typed = query.trim().lowercase()
            if (typed.isNotEmpty()) {
                // Foods whose words start with what was typed come first, then those that only contain it.
                fun startsWith(food: FirstFood) = food.name.lowercase().split(' ').any { it.startsWith(typed) } || food.stems.any { it.trim().startsWith(typed) }
                val matches = (hundredFoods.filter(::startsWith) + hundredFoods.filter { it.name.lowercase().contains(typed) }).distinct()
                    .filter { food -> foods.none { it.equals(food.name, ignoreCase = true) } }.take(8)
                if (matches.isNotEmpty()) {
                    SectionLabel("Fra 100 før 1")
                    FoodChips(matches.map { label(it) to it.name }, accent) { add(it) }
                }
                if (hundredFoods.none { it.name.equals(query.trim(), ignoreCase = true) }) FlowRow {
                    AssistChip(onClick = { add(query.trim()) }, label = { Text("Tilføj \"${query.trim()}\" som egen") }, leadingIcon = { Icon(Icons.Outlined.Add, null, Modifier.size(16.dp)) })
                }
            } else if (events.isNotEmpty()) {
                // Without the history (when editing from the journal) only the search is shown.
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("100 før 1 · ${progress.size} af 100 smagt", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (onOpenList != null) TextButton(onClick = onOpenList) { Text("Se hele listen") }
                }
                // One of each allergen first, so they come early, then the rest of the list in order.
                val open = hundredFoods.filter { it !in progress && foods.none { f -> f.equals(it.name, ignoreCase = true) } }
                val untasted = (open.filter { it.allergen != null }.distinctBy { it.allergen }.take(4) + open.filter { it.allergen == null }).take(8)
                if (untasted.isNotEmpty()) {
                    SectionLabel("Ikke smagt endnu")
                    FoodChips(untasted.map { (if (it.allergen != null) "${it.name} · allergen" else it.name) to it.name }, accent) { add(it) }
                }
                val recentShown = recent.filter { r -> foods.none { it.equals(r, ignoreCase = true) } }
                if (recentShown.isNotEmpty()) {
                    SectionLabel("Senest brugt")
                    FoodChips(recentShown.map { it to it }, accent) { add(it) }
                }
            }
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
    }, confirmButton = { Button(enabled = foodName().isNotBlank(), onClick = { onSave(draft.copy(foodName = foodName())) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
private fun SectionLabel(text: String) = Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

/** Tappable food suggestions; each pair is the shown label and the food name that is added. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodChips(items: List<Pair<String, String>>, accent: androidx.compose.ui.graphics.Color, onPick: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { (label, food) ->
            AssistChip(onClick = { onPick(food) }, label = { Text(label) }, colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.onSurface, leadingIconContentColor = accent))
        }
    }
}

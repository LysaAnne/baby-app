package dk.babyapp.ui.tracking

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import dk.babyapp.ui.theme.careCategory
import dk.babyapp.data.preferences.orderedQuickActionCategories
import dk.babyapp.data.preferences.QuickActionCategory
import dk.babyapp.ui.theme.babyColors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun RegistrationTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null, placeholder: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false, enabled: Boolean = true, singleLine: Boolean = false,
    isError: Boolean = false, minLines: Int = 1, maxLines: Int = Int.MAX_VALUE,
    trailingIcon: @Composable (() -> Unit)? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    // Full width by default, so every form lines up; a weight or width from the caller still applies first.
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), label = label, placeholder = placeholder, readOnly = readOnly,
        enabled = enabled, singleLine = singleLine, isError = isError, minLines = minLines, maxLines = maxLines,
        trailingIcon = trailingIcon, keyboardOptions = keyboardOptions, shape = MaterialTheme.shapes.small,
        colors = MaterialTheme.babyColors.let { field ->
            OutlinedTextFieldDefaults.colors(focusedContainerColor = field.fieldContainer, unfocusedContainerColor = field.fieldContainer,
                disabledContainerColor = field.fieldContainer, errorContainerColor = field.fieldContainer,
                cursorColor = field.fieldIcon, errorTextColor = field.fieldText, disabledTextColor = field.fieldIcon,
                focusedTextColor = field.fieldText, unfocusedTextColor = field.fieldText,
                focusedLabelColor = field.fieldLabelFocused, unfocusedLabelColor = field.fieldLabel,
                focusedTrailingIconColor = field.fieldIcon, unfocusedTrailingIconColor = field.fieldIcon,
                focusedPlaceholderColor = field.fieldPlaceholder, unfocusedPlaceholderColor = field.fieldPlaceholder)
        })
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun ManualRegistrationMenu(options: List<Pair<String, () -> Unit>>, onDismiss: () -> Unit) {
    fun category(label: String) = when (label) {
        "Amning", "Flaske", "Pumpning" -> QuickActionCategory.Feeding
        "Ble" -> QuickActionCategory.Diaper
        "Søvn" -> QuickActionCategory.Sleep
        "Mål", "Vægt", "Højde", "Hovedomkreds", "Temperatur" -> QuickActionCategory.Measurements
        "Fast føde" -> QuickActionCategory.SolidFood
        "Diverse", "Mavetid", "Bad", "Leg", "Udetid", "Andet" -> QuickActionCategory.Misc
        else -> QuickActionCategory.Health
    }
    val groups = orderedQuickActionCategories(emptyList()).map { it to options.filter { option -> category(option.first) == it } }.filter { it.second.isNotEmpty() }
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss, sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Manuel registrering", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 4.dp))
            // A category with one choice gets no heading of its own (a heading "Ble" over a button "Ble" reads oddly);
            // its button carries the category icon instead, and such buttons share rows.
            val singles = mutableListOf<Pair<QuickActionCategory, Pair<String, () -> Unit>>>()
            @Composable fun flushSingles() {
                if (singles.isEmpty()) return
                val rows = singles.toList().chunked(2)
                singles.clear()
                Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (group, option) ->
                                FilledTonalButton(onClick = option.second, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                    Icon(group.careCategory.icon, contentDescription = null, tint = MaterialTheme.babyColors.category(group.careCategory).accent, modifier = Modifier.size(18.dp))
                                    Text(" " + option.first, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            groups.forEach { (group, items) ->
                if (items.size == 1) singles += group to items.single() else {
                    flushSingles()
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(group.careCategory.icon, contentDescription = null, tint = MaterialTheme.babyColors.category(group.careCategory).accent, modifier = Modifier.size(20.dp))
                        Text(group.label, style = MaterialTheme.typography.titleSmall)
                    }
                    items.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (label, select) -> QuickButton(label, Modifier.weight(1f), onClick = select) }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            flushSingles()
        }
    }
}

internal fun manualActivityTypeFor(label: String): dk.babyapp.data.tracking.ActivityType =
    dk.babyapp.data.tracking.ActivityType.entries.firstOrNull { it != dk.babyapp.data.tracking.ActivityType.Medicine && it.displayLabel() == label }
        ?: dk.babyapp.data.tracking.ActivityType.Other

@Composable
internal fun WeightUnitSelector(unit: String, value: String, onChange: (String, String) -> Unit) {
    ChoiceChips(listOf("g" to "Gram", "kg" to "Kg"), unit) { picked ->
        val selected = picked ?: return@ChoiceChips
        val converted = value.toDoubleOrNull()?.let { amount ->
            val result = if (unit == selected) amount else if (selected == "g") amount * 1000 else amount / 1000
            java.math.BigDecimal.valueOf(result).stripTrailingZeros().toPlainString()
        } ?: value
        onChange(selected, converted)
    }
}

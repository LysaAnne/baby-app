package dk.babyapp.ui.tracking

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
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
            // Every category looks the same: its coloured icon on the left and its buttons beside it, two per row.
            groups.forEachIndexed { index, (group, items) ->
                val colors = MaterialTheme.babyColors.category(group.careCategory)
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 4.dp).size(36.dp).background(colors.container, androidx.compose.foundation.shape.CircleShape).semantics { contentDescription = group.label }, contentAlignment = Alignment.Center) {
                        Icon(group.careCategory.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { (label, select) -> QuickButton(label, Modifier.weight(1f), onClick = select) }
                            }
                        }
                    }
                }
            }
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

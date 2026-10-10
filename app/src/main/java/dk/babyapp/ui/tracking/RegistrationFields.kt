package dk.babyapp.ui.tracking

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
    OutlinedTextField(value, onValueChange, modifier, label = label, placeholder = placeholder, readOnly = readOnly,
        enabled = enabled, singleLine = singleLine, isError = isError, minLines = minLines, maxLines = maxLines,
        trailingIcon = trailingIcon, keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White, errorContainerColor = Color.White,
            cursorColor = Color.DarkGray, errorTextColor = Color(0xFF202520), disabledTextColor = Color.DarkGray,
            focusedTextColor = Color(0xFF202520), unfocusedTextColor = Color(0xFF202520),
            focusedLabelColor = Color(0xFF364B3D), unfocusedLabelColor = Color(0xFF4A514C),
            focusedTrailingIconColor = Color.DarkGray, unfocusedTrailingIconColor = Color.DarkGray,
            focusedPlaceholderColor = Color.Gray, unfocusedPlaceholderColor = Color.Gray))
}

@Composable
internal fun ManualRegistrationMenu(options: List<Pair<String, () -> Unit>>, onDismiss: () -> Unit) {
    fun category(label: String) = when (label) {
        "Amning", "Flaske", "Pumpning" -> "Madning"
        "Ble" -> "Ble"
        "Søvn" -> "Søvn"
        "Mål", "Vægt", "Højde", "Hovedomkreds", "Temperatur" -> "Mål"
        "Fast føde" -> "Fast føde"
        "Diverse", "Mavetid", "Bad", "Leg", "Udetid", "Andet" -> "Diverse"
        else -> "Sundhed"
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Manuel registrering") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dk.babyapp.data.preferences.orderedQuickActionCategories(emptyList()).forEach { title ->
                val items = options.filter { category(it.first) == title }
                if (items.isNotEmpty()) OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        items.forEach { (label, select) -> FilledTonalButton(onClick = select, modifier = Modifier.fillMaxWidth()) { Text(label) } }
                    }
                }
            }
        }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Luk") } })
}

internal fun manualActivityTypeFor(label: String): dk.babyapp.data.tracking.ActivityType =
    dk.babyapp.data.tracking.ActivityType.entries.firstOrNull { it != dk.babyapp.data.tracking.ActivityType.Medicine && it.displayLabel() == label }
        ?: dk.babyapp.data.tracking.ActivityType.Other
